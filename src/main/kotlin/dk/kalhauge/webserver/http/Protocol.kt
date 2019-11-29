package dk.kalhauge.webserver.http

import dk.kalhauge.utils.readLine
import dk.kalhauge.utils.readString
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

abstract class Protocol {
  enum class MimeType(val code: String) {
    RAW("application/octet-stream"),
    HTML("text/html"),
    JSON("application/json"),
    TEXT("text/plain");
    companion object {
      operator fun get(code: String?) = values().firstOrNull { it.code == code } ?: RAW
      }
    }
  class Header(val label: String, var value: String) {
    val key: String
      get() =
          label.toLowerCase().split("-").map { it.capitalize() }.joinToString("-")
    operator fun get(code: String) =
      value
          .split("; *".toRegex())
          .map { it.split("=") }
          .filter { (it.size == 1 && code == "") || code ==  it[0] }
          .map { it.last() }
          .firstOrNull()
    }
  val headers = mutableMapOf<String, Header>()
  abstract val contentLength: Int
  abstract val contentType: MimeType
  abstract val charset: Charset

  }

class Request(val input: InputStream) : Protocol() {
  enum class Method { GET, PUT, POST, DELETE }

  val method: Method
  val resource: String

  init {
    var line = input.readLine()
    val parts = line.split(" ")
    if (parts.size != 3 || !parts[2].startsWith("HTTP/"))
        throw UnsupportedOperationException("Unknown protocol")
    method = Method.valueOf(parts[0])
    resource = parts[1]
    line = input.readLine()
    while (line.isNotEmpty()) {
      val headerParts = line.split(":")
      val header = Header(headerParts[0].trim(), headerParts[1].trim())
      headers[header.key] = header
      line = input.readLine()
      }
    }

  override val contentLength = headers["Content-Length"]?.value?.toIntOrNull() ?: 0
  override val contentType = MimeType[headers["Content-Type"]?.get("")]
  override val charset = charset(headers["Content-Type"]?.get("charset") ?: "UTF-8")

  val body = input.readString(contentLength, charset)
  }

open class Response() : Protocol() {
  enum class Status(val code: Int, val message: String) {
    OK(200, "OK"),
    NOT_FOUND(404, "Not found");
    }

  override val contentLength: Int
    get() = TODO("not implemented")
  override val contentType: MimeType
    get() = TODO("not implemented")
  override val charset: Charset
    get() = TODO("not implemented")
  }


fun main() {
  val data = """
      GET /member/7 HTTP/1.1
      Content-Type: text/plain; charset=UTF-8
      Content-Length: 14
      
      Hellå
      Wørld!
      xxx
      
      """.trimIndent()
  val input = data.byteInputStream()
  val request = Request(input)
  println("${request.method} ${request.resource}")
  println("----------")
  println("${request.body}")
  println(request.contentType)

}