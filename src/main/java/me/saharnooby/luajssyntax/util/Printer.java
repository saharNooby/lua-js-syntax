package me.saharnooby.luajssyntax.util;

import lombok.NonNull;
import lombok.Setter;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author saharNooby
 * @since 19:01 21.08.2019
 */
public final class Printer {

	private final Appendable destination;

	private int line = 1;
	@Setter
	private int expectedLine;

	public Printer(@NonNull Appendable destination) {
		this.destination = destination;
	}

	public void print(String s) {
		ensureCorrectLine();
		append(s);
	}

	public void println(String s) {
		if (!s.isEmpty()) {
			print(s);
		}

		append("\n");

		this.line++;
	}

	private void ensureCorrectLine() {
		while (this.expectedLine > this.line) {
			println("");
		}
	}

	private void append(String s) {
		try {
			this.destination.append(s);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
