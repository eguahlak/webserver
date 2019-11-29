package dk.kalhauge.utils

import java.io.InputStream
import java.io.OutputStream
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.charset.Charset

const val EOF = -1
const val EOT = 4
const val LF = 10
const val CR = 13

/**
 * Reading a line from an input stream directly into a string.
 *
 * Using the fact that LF and CR are single byte characters in
 * most charsets and that they can never be a continuation character in
 * UTF-8 it is easy to detect the end of a line.
 *
 * CR (carriage return) might follow or precede LF (line feed)
 */
fun InputStream.readLine(charset: Charset = Charsets.UTF_8): String {

  fun StringBuilder.consume(bytes: ByteBuffer): StringBuilder {
    bytes.limit(bytes.position()).position(0)
    this.append(charset.decode(bytes))
    bytes.clear()
    return this
    }

  val result = StringBuilder()
  val bytes = ByteBuffer.allocate(256)
  while (true) {
    val byte = this.read()
    if (byte == LF || byte == EOF || byte == EOT) return result.consume(bytes).toString()
    if (byte == CR) continue // ignore CRs
    if (bytes.position() > bytes.limit() - 4 && charset.isStart(byte)) result.consume(bytes)
    bytes.put(byte.toByte())
    }
  throw RuntimeException("No line ending found")
  }

fun InputStream.readBytes(count: Int) = ByteArray(count).also { read(it, 0, count) }

fun InputStream.readString(count: Int, charset: Charset = Charsets.UTF_8) =
    charset.decode(ByteBuffer.wrap(readBytes(count))).toString()

/**
 * Is this byte a continuation to the previous byte(s).
 * In some charsets as UTF-8 a character uses more than one byte,
 * all bytes that is not the first of those bytes are continuations
 *
 * @param byte the byte as an integer to check
 * @return whether this byte is a continuation byte
 */
fun Charset.isContinuation(byte: Int) =
  when (this) {
    Charsets.UTF_8 -> byte and 0b1100_0000 == 0b1000_0000
    Charsets.UTF_16 -> byte != 0 // this is an approximation
    else -> false
    }

fun Charset.isStart(byte: Int) = !isContinuation(byte)

fun OutputStream.writeLine(line: String, charset: Charset = Charsets.UTF_8) =
    apply {
      write(line).write(LF)
      }

fun OutputStream.writeLine() = apply { write(LF) }

fun OutputStream.write(string: String, charset: Charset = Charsets.UTF_8) =
    apply {
      val buffer = charset.encode(string)
      write(buffer.array(), 0, buffer.limit())
      }

