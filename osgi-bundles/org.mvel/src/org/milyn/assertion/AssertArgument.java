/*
 Milyn - Copyright (C) 2006

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License (version 2.1) as published by the Free Software
 Foundation.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

 See the GNU Lesser General Public License for more details:
 http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.assertion;

/**
 * Argument assertion utilities.
 *
 * @author tfennelly
 */
public abstract class AssertArgument {

	/**
	 * Assert that the argument is not null.
	 *
	 * @param arg
	 *            Argument.
	 * @param argName
	 *            Argument name.
	 * @throws IllegalArgumentException
	 *             Argument is null.
	 */
	public static void isNotNull(Object arg, String argName)
			throws IllegalArgumentException {
		if (arg == null) {
			throw new IllegalArgumentException("null '" + argName
					+ "' arg in method call.");
		}
	}

	/**
	 * Assert that the argument is not empty.
	 *
	 * @param arg
	 *            Argument.
	 * @param argName
	 *            Argument name.
	 * @throws IllegalArgumentException
	 *             Argument is not null, but is empty.
	 */
	public static void isNotEmpty(String arg, String argName)
			throws IllegalArgumentException {
		if (arg != null && arg.trim().length() == 0) {
			throw new IllegalArgumentException("Not null, but empty '"
					+ argName + "' arg in method call.");
		}
	}

	/**
	 * Assert that the argument is neither null nor empty.
	 *
	 * @param arg
	 *            Argument.
	 * @param argName
	 *            Argument name.
	 * @throws IllegalArgumentException
	 *             Argument is null or empty.
	 */
	public static void isNotNullAndNotEmpty(String arg, String argName)
			throws IllegalArgumentException {
		if (arg == null || arg.trim().length() == 0) {
			throw new IllegalArgumentException("null or empty '" + argName
					+ "' arg in method call.");
		}
	}
}
