<?xml version="1.0" encoding="UTF-8"?>
  <document xmlns:gsp='http://groovy.codehaus.org/2005/gsp' xmlns:foo='baz' type='letter'>
  <gsp:scriptlet>def greeting = "${context.get('foo')}est"</gsp:scriptlet>
  <gsp:expression>greeting</gsp:expression>
  <foo:to>${context.get('foo')} "${context.get('foo')}" ${context.get('foo')}</foo:to>
  How are you today?
</document>