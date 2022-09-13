package me.pride.spirits.api.exception;

public class InvalidSpiritTypeException extends Exception {
	@Deprecated
	public InvalidSpiritTypeException() {
		super("Cannot build spirit; spirit type provided is not supported by the operation you are trying to perform.");
	}
}
