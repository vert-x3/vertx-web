package io.vertx.ext.web.impl;

public class ParsedWeightedMIME extends ParsedMIME implements Comparable<ParsedMIME> {

  private double weight;

  public ParsedWeightedMIME(String mimeComponent, String mimeSubComponent, double weight) {
    super(mimeComponent, mimeSubComponent);
    this.weight = weight;
  }
  
  public ParsedWeightedMIME weight(double weight) {
    this.weight = weight;
    return this;
  }
  public double weight() {
    return weight;
  }

  @Override
  public int compareTo(ParsedMIME otherObj) {
    if(otherObj instanceof ParsedWeightedMIME){
      ParsedWeightedMIME other = (ParsedWeightedMIME) otherObj;
      int compared = Double.compare(other.weight, weight);
      if(compared != 0){
        return compared;
      }
    }
    return super.compareTo(otherObj);
  }
  
}
