package io.vertx.ext.web.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MIMEParser {
  
  private static final double DEFAULT_WEIGHT = 1.0;
  // TODO investigate if something more complex is required as this only follows the RFC partly
  private static final Pattern COMMA_SPLITTER = Pattern.compile(" *, *");
  private static final Pattern PARAMETER_FINDER =
      Pattern.compile("\\s*;\\s*(?<key>[a-zA-Z0-9]+)\\s*(?:=\\s*(?<value>(?:[a-zA-Z0-9.@#%-_]|\"[^\"]+\")+))?");
  
  public static ParsedMIME parseMIMEType(String unparsedMIME){
    int slashIndex = unparsedMIME.indexOf('/');
    int paramIndex = unparsedMIME.indexOf(';');
    
    String component = unparsedMIME.substring(0, slashIndex);
    String subcomponent; 

    if(paramIndex < 0){
      subcomponent = unparsedMIME.substring(slashIndex + 1);
    } else {
      subcomponent = unparsedMIME.substring(slashIndex + 1, paramIndex);
    }
    
    ParsedWeightedMIME parsedMIME = new ParsedWeightedMIME(component, subcomponent, DEFAULT_WEIGHT);
    
    if(paramIndex > 0){
      Matcher paramFindings = PARAMETER_FINDER.matcher(unparsedMIME);
      
      while(paramFindings.find()){
        String key = paramFindings.group("key");
        // If "q" doesn't have a double as a value, it is ignored on purpose!
        if("q".equals(key)){
          try{
            parsedMIME.weight(Double.parseDouble(paramFindings.group("value")));
          }catch(NumberFormatException e){
            // MYTODO Log as debug this happened
          }
        } else {
          String value = paramFindings.group("value");
          if(value == null){
            parsedMIME.addParameter(paramFindings.group("key"));
          } else {
            parsedMIME.addParameter(paramFindings.group("key"), value);
          }
        }
      }
    }
    
    return parsedMIME;
    
  }
  
  public static List<ParsedMIME> parseMIMETypes(String unparsedMIMEs){
    
    String[] listedMIMEs = COMMA_SPLITTER.split(unparsedMIMEs);
    
    List<ParsedMIME> parsedMIMEs = new ArrayList<>(listedMIMEs.length);
    
    for (String listedMIME : listedMIMEs) {
      parsedMIMEs.add(parseMIMEType(listedMIME));
    }
    
    return parsedMIMEs;
  }
  
}
