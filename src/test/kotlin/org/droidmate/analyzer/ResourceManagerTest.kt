package org.droidmate.analyzer

import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters

/**
 * Unit tests for the resource manager class
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4::class)
class ResourceManagerTest {
    @Test
    fun InitializeFilledListTest(){
        val apiListFilled = ResourceManager().initializeApiMapping("test_list_filled.txt")

        assertTrue(apiListFilled.size == 3)
        assertTrue(apiListFilled[0].toString() == "android.hardware.Camera->open(int)\t")
        assertTrue(apiListFilled[1].toString() == "android.media.AudioRecord-><init>(int,int,int,int,int)\t")
        assertTrue(apiListFilled[2].toString() == "android.content.ContentResolver->query(android.net.Uri,java.lang.String[],java.lang.String,java.lang.String[],java.lang.String,android.os.CancellationSignal)\tcontent://call_log/calls")
    }

    @Test
    fun InitializeEmptyListFilledTest(){
        try {
            ResourceManager().initializeApiMapping("test_list_empty.txt")
            Assert.fail()
        }
        catch(e : AssertionError){
            assertTrue(true)
        }
    }
}