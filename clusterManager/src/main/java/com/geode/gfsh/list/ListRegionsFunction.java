package com.geode.gfsh.list;

import java.util.Properties;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.geode.gfsh.ExceptionHelpers;
import com.geode.gfsh.GfshCommand;
import com.geode.gfsh.function.GfshFunction;

public class ListRegionsFunction extends GfshFunction {
  private static final long serialVersionUID = 1L;

  /** logWriter for writing to the server log */
    private final transient LogWriter logWriter;
   
    private transient GfshCommand gfshCommand;

    public ListRegionsFunction() {
        Cache cache = CacheFactory.getAnyInstance();
        logWriter = cache.getLogger();
        gfshCommand = new GfshCommand(logWriter);
    }

    @Override
    public void execute(FunctionContext context) {
      try {
        Object arguments = context.getArguments();
        if (arguments == null || !(arguments instanceof String)) {
          throw new RuntimeException("One string required as an argument");
        }

        // run gfsh function here
        
        // parse results
        
        String command = (String) arguments;

        logWriter.fine("Gfsh command: " + command);

        String commandResult = gfshCommand.execute(command);
        
//        List<String> parsedResults = gfshCommand.parseResults(command, gfshResults);

        logWriter.info("Gfsh command successful: " + command);

        context.getResultSender().lastResult(command);

      } catch (Exception exception) {
    	  ExceptionHelpers.sendStrippedException(context.getResultSender(), exception, logWriter);
      }
    }
    
    /*
     * (non-Javadoc)
     * @see com.gemstone.gemfire.cache.execute.Function#getId()
     */
    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    /*
     * (non-Javadoc)
     * @see com.gemstone.gemfire.cache.execute.Function#optimizeForWrite()
     */
    @Override
    public boolean optimizeForWrite() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.gemstone.gemfire.cache.execute.Function#isHA()
     */
    @Override
    public boolean isHA() {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.gemstone.gemfire.cache.execute.Function#hasResult()
     */
   @Override
   public boolean hasResult() {
      return true;
    }

    /*
     * (non-Javadoc)
     * @see com.gemstone.gemfire.cache.Declarable#init(java.util.Properties)
     */
    public void init(final Properties properties) {
    }
}
