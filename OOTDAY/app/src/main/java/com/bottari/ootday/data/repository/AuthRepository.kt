package com.bottari.ootday.data.repository

import android.util.Log
import com.bottari.ootday.data.model.signupModel.SignUpData
import com.bottari.ootday.data.service.AuthApiService
import com.bottari.ootday.data.service.MemberApiService
import com.bottari.ootday.data.service.SmsApiService
import com.bottari.ootday.domain.model.DataStoreManager
import com.bottari.ootday.domain.repository.dataClass.AuthCheckResponse
import com.bottari.ootday.domain.repository.dataClass.PhoneNumberAuthResponse
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import com.bottari.ootday.data.model.loginModel.LoginRequest
import kotlinx.coroutines.flow.first

sealed class LoginFlowResult {
    data object NavigateToMain : LoginFlowResult()
    data object NavigateToSurvey : LoginFlowResult()
}

class AuthRepository(private val context: Context) {
    // Retrofit 인스턴스 (싱글톤으로 관리하거나 Dagger Hilt 등으로 주입받는 것이 좋습니다)
    private val baseUrl = "https://temp.example.com/api/"

    private val dataStoreManager = DataStoreManager(context)

    private val smsApiService: SmsApiService by lazy {
        RetrofitClient.createService(SmsApiService::class.java)
    }

    private val memberApiService: MemberApiService by lazy {
        RetrofitClient.createService(MemberApiService::class.java)
    }

    init {
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    //로그인 모킹함수
//    suspend fun login(
//        username: String,
//        password: String,
//    ): Result<Boolean> {
//        // TODO: 실제 API 호출 로직 구현
//        // 예시: ApiService를 통한 서버 통신
//
//        // 임시 구현 (실제로는 서버 응답에 따라 처리)
//        return if (username == "test" && password == "1234") {
//            Result.success(true)
//        } else {
//            Result.failure(Exception("아이디 혹은 비밀번호가 잘못되었습니다. 다시 시도해주세요."))
//        }
//    }

    suspend fun login(request: LoginRequest): Result<Unit> {
        return try {
            val loginResponse = memberApiService.login(request)
            if (!loginResponse.isSuccessful || loginResponse.body() == null) {
                return Result.failure(Exception("아이디 또는 비밀번호가 일치하지 않습니다."))
            }

            val token = loginResponse.body()!!.token
            // 토큰 및 로그인 유지 상태 저장
            dataStoreManager.saveToken(token)
            dataStoreManager.saveRememberMe(request.rememberMe)
            Result.success(Unit) // 성공만 반환
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

//    // 휴대폰 번호 인증 요청 모킹 함수
//    suspend fun requestPhoneNumberAuth(phoneNumber: String): Result<PhoneNumberAuthResponse> {
//        // 백엔드 명세가 나오기 전까지 실제 API 호출 대신 가짜 응답을 반환합니다.
//        delay(500) // 실제 네트워크 지연을 흉내내기 위해 잠시 대기합니다.
//        return Result.success(PhoneNumberAuthResponse(true, "인증번호가 모의 발송되었습니다."))
//    }
//
//    // 인증 번호 확인 모킹 함수
//    suspend fun checkPhoneNumberAuth(
//        phoneNumber: String,
//        authCode: String,
//    ): Result<AuthCheckResponse> {
//        // 현재는 어떤 인증번호를 입력해도 성공으로 처리
//        delay(300) // 실제 네트워크 지연을 흉내내기 위해 잠시 대기
//        val isCodeValid = authCode == "123456" // 예시: "123456"을 올바른 인증번호로 가정
//        return if (isCodeValid) {
//            Result.success(AuthCheckResponse(true, "인증되었습니다."))
//        } else {
//            Result.success(AuthCheckResponse(false, "인증번호가 일치하지 않습니다.")) // 인증 실패 시 메시지
//        }
//    }

    suspend fun requestPhoneNumberAuth(phoneNumber: String): Result<String> {
        return try {
            val response = smsApiService.sendSmsVerification(phoneNumber)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string() // 에러 본문 읽기
                Log.e("AuthRepository", "API Error - Code: $errorCode, Body: $errorBody")
                Result.failure(Exception("서버 응답 오류 (코드: $errorCode)"))
            }
        } catch (e: Exception) {
            // 네트워크 오류 등 예외 발생 시
            Result.failure(e)
        }
    }

    // 인증 번호 확인 함수 (이 함수도 필요하다면 모킹할 수 있습니다.)
    suspend fun checkPhoneNumberAuth(phoneNumber: String, code: String): Result<String> {
        return try {
            val response = smsApiService.verifySmsCode(phoneNumber, code)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "API Error - Code: $errorCode, Body: $errorBody")
                Result.failure(Exception("인증번호가 일치하지 않습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(signUpData: SignUpData): Result<String> {
        return try {
            // ✨ 호출하는 서비스 인스턴스 이름 변경
            val response = memberApiService.join(signUpData)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "SignUp Error - Code: ${response.code()}, Body: $errorBody")
                Result.failure(Exception("회원가입 실패 (코드: ${response.code()})"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "SignUp Exception", e)
            Result.failure(e)
        }
    }

    // SplashActivity에서 사용할 토큰 유효성 검사 함수
    suspend fun validateToken(): Boolean {
        val token = dataStoreManager.getToken.first()
        val rememberMe = dataStoreManager.getRememberMe.first()
        if (!rememberMe || token.isNullOrBlank()) return false

        return try {
            val response = memberApiService.getMyProfile("Bearer $token")
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }



}