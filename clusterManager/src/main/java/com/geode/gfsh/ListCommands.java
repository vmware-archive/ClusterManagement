package com.geode.gfsh;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.management.internal.cli.json.GfJsonException;
import com.gemstone.gemfire.management.internal.cli.result.CommandResult;
import com.geode.gfsh.list.ListCommandsParser;

public class ListCommands {

	private GfshCommand gfshCommand;
	private ListCommandsParser parser = new ListCommandsParser();
	
	public ListCommands(LogWriter logWriter) {
		gfshCommand = new GfshCommand(logWriter);
	}

	public String execute(String command)
			throws IllegalAccessException, 
			  IllegalArgumentException, 
			  InvocationTargetException {
		
		return gfshCommand.execute(command);
	}

	public List<String> parseResults(CommandResult commandResult, String listTarget) throws GfJsonException {
		return this.parseResults(commandResult, listTarget, null);
	}

	public List<String> parseResults(CommandResult commandResult, String listTarget, Object listArguments) {

		String regexExpression = null;
		if (listArguments == null) {
			;
		}
		if (listArguments instanceof String) {
			regexExpression = (String) listArguments;
		}
	
	List<String> listResults = new ArrayList<String>();
	
	switch(listTarget) {
		case "regions" :
			if (listArguments == null) {
				return parser.listRegions(commandResult);
			}
			else {
				return parser.listRegionsLike(commandResult, regexExpression);
			}
		}
	return listResults;
	}

	public List<String> parseResults(String commandResult, String listTarget, Object listArguments) {

		String regexExpression = null;
		if (listArguments == null) {
			;
		}
		if (listArguments instanceof String) {
			regexExpression = (String) listArguments;
		}
	
	List<String> listResults = new ArrayList<String>();
	
	switch(listTarget) {
		case "regions" :
			if (listArguments == null) {
				return parser.listRegions(commandResult);
			}
			else {
				return parser.listRegionsLike(commandResult, regexExpression);
			}
		}
	return listResults;
	}

}
