package me.pride.spirits.api.exception;

public class InvalidSpiritTypeException extends Exception {
	public InvalidSpiritTypeException() {
		super("Cannot build spirit; not a valid spirit type.");
	}
}
