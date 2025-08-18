package com.bottari.ootday.domain.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(val context: Context) {
    companion object {
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        val KEY_REMEMBER_ME = booleanPreferencesKey("remember_me") // 로그인 유지 키 추가
    }

    // 토큰 저장
    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[KEY_AUTH_TOKEN] = token }
    }

    // 로그인 유지 상태 저장
    suspend fun saveRememberMe(isRemembered: Boolean) {
        context.dataStore.edit { it[KEY_REMEMBER_ME] = isRemembered }
    }

    // 토큰 불러오기
    val getToken: Flow<String?> = context.dataStore.data.map { it[KEY_AUTH_TOKEN] }

    // 로그인 유지 상태 불러오기
    val getRememberMe: Flow<Boolean> = context.dataStore.data.map { it[KEY_REMEMBER_ME] ?: false }

    // 로그아웃 시 모든 데이터 삭제
    suspend fun clearData() {
        context.dataStore.edit { it.clear() }
    }
}