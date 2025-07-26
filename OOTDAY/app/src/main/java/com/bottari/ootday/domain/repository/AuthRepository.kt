package com.bottari.ootday.domain.repository

class AuthRepository {

    suspend fun login(username: String, password: String): Result<Boolean> {
        // TODO: 실제 API 호출 로직 구현
        // 예시: ApiService를 통한 서버 통신

        // 임시 구현 (실제로는 서버 응답에 따라 처리)
        return if(username == "test" && password == "1234") {
            Result.success(true)
        } else {
            Result.failure(Exception("아이디 혹은 비밀번호가 올바르지 않습니다."))
        }
    }

    // 로그인 상태 저장/불러오기 (SharedPreferences 사용)
    fun saveLoginState(isLoggedIn: Boolean) {
        // TODO: SharedPreferences에 로그인 상태 저장
    }

    fun isLoggedIn(): Boolean {
        // TODO: SharedPreferences에서 로그인 상태 확인
        return false
    }

    // 자동 로그인을 위한 토큰 저장/불러오기
    fun saveToken(token: String) {
        // TODO: 토큰 저장 로직
    }

    fun getToken(): String? {
        // TODO: 저장된 토큰 반환
        return null
    }
}