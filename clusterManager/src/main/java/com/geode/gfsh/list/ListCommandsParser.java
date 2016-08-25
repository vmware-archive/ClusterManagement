package com.geode.gfsh.list;

import java.util.ArrayList;
import java.util.List;

import com.gemstone.gemfire.management.internal.cli.json.GfJsonException;
import com.gemstone.gemfire.management.internal.cli.result.CommandResult;

/**
 * removes the gfsh text added to the result and returns a list of regions
 * 
 * @param gfshCommandResult
 * @return
 */
public class ListCommandsParser {

	public List<String> listRegions(CommandResult commandResult) {

		return listRegionsLike(commandResult, null);
	}


	public List<String> listRegions(String commandResult) {

		return listRegionsLike(commandResult, null);
	}

	/**
	 * removes the gfsh text added to the result and returns a list of regions
	 * that match a regex pattern.
	 * 
	 * @param gfshCommandResult
	 * @param regexPattern
	 * @return
	 * @throws GfJsonException
	 */
	public List<String> listRegionsLike(CommandResult commandResult, String regexPattern) {

		List<String> regionNames = new ArrayList<String>();

		commandResult.resetToFirstLine();
		while (commandResult.hasNextLine()) {
			String resultLine = commandResult.nextLine();
			String[] resultLines = resultLine.split(System.getProperty("line.separator"));
			for (String regionName : resultLines) {
				if (regionName.startsWith("List of") || regionName.startsWith("----------")) {
					continue;
				}
				if (regexPattern == null || regionName.matches(regexPattern)) {
					regionNames.add(regionName);
				}
			}
		}
		return regionNames;
	}

	/**
	 * removes the gfsh text added to the result and returns a list of regions
	 * that match a regex pattern.
	 * 
	 * @param gfshCommandResult
	 * @param regexPattern
	 * @return
	 * @throws GfJsonException
	 */
	public List<String> listRegionsLike(String commandResult, String regexPattern) {

		List<String> regionNames = new ArrayList<String>();

		String[] resultLines = commandResult.split(System.getProperty("line.separator"));
		for (String regionName : resultLines) {
			if (regionName.startsWith("List of") || regionName.startsWith("----------")) {
				continue;
			}
			if (regexPattern == null || regionName.matches(regexPattern)) {
				regionNames.add(regionName);
			}
		}
		return regionNames;
	}

	public List<String> parse(String listCommand, String gfshCommandResults, Object arguments) {
		List<String> results = new ArrayList<String>();

		// switch (listCommand) {
		// case "regions" :
		// List<String> parsedResults = listRegions(gfshCommandResults);
		//
		// }
		return results;
	}
}
