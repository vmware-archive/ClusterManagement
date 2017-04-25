package com.geode.gfsh;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.geode.LogWriter;
import org.apache.geode.management.cli.CommandProcessingException;
import org.apache.geode.management.internal.cli.CommandManager;
import org.apache.geode.management.internal.cli.GfshParser;
import org.apache.geode.management.internal.cli.result.CommandResult;
import org.springframework.shell.event.ParseResult;

public class GfshCommand {

	private LogWriter logWriter;

	private CommandManager commandManager;
	private GfshParser gfshParser;

	public GfshCommand(final LogWriter logWriter) {
		this.logWriter = logWriter;

		try {
			commandManager = CommandManager.getInstance();
		} catch (ClassNotFoundException | IOException e) {
			logWriter.error("Error instantiating a GemFire Command Manager. Internal error. Check logs", e);
			throw new RuntimeException("Error instantiating a GemFire Command Manager. Internal error. Check logs", e);
		}
		gfshParser = new GfshParser(commandManager);
	}

	/**
	 * Don't throw exceptions back to the client, as we don't know if this call
	 * was synch or asynch. Logging an error will be sufficient.
	 */
	public String execute(final String command)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, RuntimeException {
		CommandResult commandResult = executeInternal(command);
		return resultToString(commandResult);
	}

	/**
	 * Don't throw exceptions to a listener event method, as they're not passed
	 * back to the client that triggered the event. Logging an error will be
	 * sufficient.
	 */
	public CommandResult executeInternal(final String gfshCommand)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, RuntimeException {
		try {
			ParseResult parseResult = gfshParser.parse(gfshCommand);
			logWriter.fine("Executing the GFSH command");
			CommandResult commandResult = (CommandResult) parseResult.getMethod().invoke(parseResult.getInstance(),
					parseResult.getArguments());
			return commandResult;
		} catch (CommandProcessingException e) {
			throw new RuntimeException(
					"Gfsh could not execute your command.\n"
					+ "Carefully check the spelling of your properties and verify that the property is valid for the command.\n",
					e);
		}
	}

	/*
	 * Translate the command result to a String
	 */
	private String resultToString(CommandResult commandResult) {
		StringBuilder builder = new StringBuilder();
		commandResult.resetToFirstLine();
		while (commandResult.hasNextLine()) {
			builder.append(commandResult.nextLine()).append(System.getProperty("line.separator"));
		}
		logWriter.fine("Gfsh CommandResult:" + builder.toString());

		return builder.toString();
	}
}
