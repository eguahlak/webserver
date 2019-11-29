package dk.kalhauge.webserver.http

import org.junit.Assert
import org.junit.Test

class ProtocolTest {

  @Test
  fun testHeaderKey() {
    val header = Protocol.Header("conTeNT-length", "7")
    Assert.assertEquals("Content-Length", header.key)
    }
  }