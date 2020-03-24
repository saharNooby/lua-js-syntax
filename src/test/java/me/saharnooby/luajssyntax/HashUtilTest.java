package me.saharnooby.luajssyntax;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author saharNooby
 * @since 22:39 23.08.2019
 */
class HashUtilTest {

	@Test
	void test() {
		Assertions.assertEquals("1BC29B36F623BA82AAF6724FD3B16718".toLowerCase(), HashUtil.md5("md5"));
		Assertions.assertEquals("D41D8CD98F00B204E9800998ECF8427E".toLowerCase(), HashUtil.md5(""));
	}

	@Test
	void testPad() {
		Assertions.assertEquals("00411460f7c92d2124a67ea0f4cb5f85", HashUtil.md5("363"));
	}

}