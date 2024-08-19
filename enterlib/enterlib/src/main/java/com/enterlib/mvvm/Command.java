package com.enterlib.mvvm;

public abstract class Command extends BaseCommand {

	public abstract void invoke(Object invocator, Object args);
}
