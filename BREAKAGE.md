## Web Validation

### SchemaParser -> SchemaRepository

```
BodyProcessorFactory#create(SchemaParser)
ValidationHandlerBuilder#create(SchemaParser)
StyledParameterProcessorFactory#create(ParameterLocation, SchemaParser)
ParameterProcessorFactory#create(ParameterLocation, SchemaParser)
```

### Removed

```
ValidationHandler#builder(SchemaParser)
```
