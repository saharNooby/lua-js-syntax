package me.saharnooby.luajssyntax;

import lombok.NonNull;

import java.io.*;

/**
 * @author saharNooby
 * @since 16:29 24.03.2020
 */
public final class Main {

	public static void main(String[] args) throws IOException {
		StringBuilder src = new StringBuilder();

		try (Reader reader = new InputStreamReader(new BufferedInputStream(args.length > 0 ? new FileInputStream(new File(args[0])) : System.in))) {
			readAllChars(reader, src);
		}

		StringBuilder dest = new StringBuilder();

		LuaJSToLua.convert(src.toString(), dest);

		if (args.length > 1) {
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(new File(args[1])))) {
				writer.write(dest.toString());
			}
		} else {
			System.out.println(dest);
		}
	}

	private static void readAllChars(@NonNull Reader reader, @NonNull StringBuilder out) throws IOException {
		char[] buf = new char[8192];
		int read;
		while ((read = reader.read(buf)) != -1) {
			out.append(buf, 0, read);
		}
	}

}
