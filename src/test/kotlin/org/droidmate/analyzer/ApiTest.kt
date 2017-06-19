package org.droidmate.analyzer

import org.droidmate.analyzer.api.Api
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters

/**
 * Unit tests for Api class
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4::class)
class ApiTest {
    @Test
    fun BuildApiTest() {
        val api = Api.build("my.Class", "aMethod", "", "")

        assertTrue(api.toString() == "my.Class->aMethod()\t")
        assertTrue(api.uri.isEmpty())
        assertTrue(api.uriParamName.isEmpty())
        assertFalse(api.hasRestriction())

        val apiWithUri = Api.build("my.Class", "aMethod", "(android.net.Uri)", "content://test")
        assertTrue(apiWithUri.toString() == "my.Class->aMethod(android.net.Uri)\tcontent://test")
        assertTrue(apiWithUri.uri == "content://test")
        assertTrue(apiWithUri.uriParamName == "p0")
        assertFalse(apiWithUri.hasRestriction())

        val apiWithUriNoBraces = Api.build("my.Class", "aMethod", "android.net.Uri", "content://test")
        assertTrue(apiWithUriNoBraces.toString() == "my.Class->aMethod(android.net.Uri)\tcontent://test")
        assertTrue(apiWithUriNoBraces.uri == "content://test")
        assertTrue(apiWithUriNoBraces.uriParamName == "p0")
        assertFalse(apiWithUriNoBraces.hasRestriction())

        assertFalse(api == apiWithUri)
        assertTrue(apiWithUri == apiWithUriNoBraces)
    }

    @Test
    fun GetMethodNameFromMethodSignatureTest() {
        val signature1 = "java.lang.Runtime->load(java.lang.String)"
        val methodName1 = Api.getMethodNameFromSignature(signature1)
        assertTrue(methodName1 == "java.lang.Runtime->load")

        val signature2 = "android.accounts.AccountManager->getAuthTokenLabel(java.lang.String,java.lang.String,android.accounts.AccountManagerCallback,android.os.Handler)"
        val methodName2 = Api.getMethodNameFromSignature(signature2)
        assertTrue(methodName2 == "android.accounts.AccountManager->getAuthTokenLabel")

        val ctor1 = "java.lang.Runtime-><init>(java.lang.String)"
        val paramsCtor1 = Api.getMethodNameFromSignature(ctor1)
        assertTrue(paramsCtor1 == "java.lang.Runtime-><init>")

        val ctor2 = "android.accounts.AccountManager-><init>(java.lang.String,java.lang.String,android.accounts.AccountManagerCallback,android.os.Handler)"
        val paramsCtor2 = Api.getMethodNameFromSignature(ctor2)
        assertTrue(paramsCtor2 == "android.accounts.AccountManager-><init>")
    }

    @Test
    fun GetParamsFromMethodSignatureTest() {
        val signature1 = "java.lang.Runtime->load(java.lang.String)"
        val params1 = Api.getParamsFromMethodSignature(signature1)
        assertTrue(params1 == "java.lang.String")

        val signature2 = "android.accounts.AccountManager->getAuthTokenLabel(java.lang.String,java.lang.String,android.accounts.AccountManagerCallback,android.os.Handler)"
        val params2 = Api.getParamsFromMethodSignature(signature2)
        assertTrue(params2 == "java.lang.String,java.lang.String,android.accounts.AccountManagerCallback,android.os.Handler")

        val ctor1 = "java.lang.Runtime-><init>(java.lang.String)"
        val paramsCtor1 = Api.getParamsFromMethodSignature(ctor1)
        assertTrue(paramsCtor1 == "java.lang.String")

        val ctor2 = "android.accounts.AccountManager-><init>(java.lang.String,java.lang.String,android.accounts.AccountManagerCallback,android.os.Handler)"
        val paramsCtor2 = Api.getParamsFromMethodSignature(ctor2)
        assertTrue(paramsCtor2 == "java.lang.String,java.lang.String,android.accounts.AccountManagerCallback,android.os.Handler")
    }

}