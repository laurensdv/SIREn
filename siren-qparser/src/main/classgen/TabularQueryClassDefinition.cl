package org.sindice.siren.qparser.tabular.query.model;

attr "org.apache.lucene.search.Query" query with TuplePattern, Expression, Clause, TabularQuery, Value;
attr "java.util.Map" queries with TuplePattern, Expression, Clause, TabularQuery, Value;

TabularQuery  ::= {ClauseQuery} Clause:c
                | {EmptyQuery}

Clause        ::= {UnaryClause} Expression:expr
                | {BinaryClause} Expression:lhe "int":op Expression:rhe
                | {NestedClause} Clause:lhc "int":op Expression:rhe

Expression    ::= {SimpleExpression} TuplePattern:tp
                | {QueryExpression} TabularQuery:q

TuplePattern  ::= Value*

Value         ::=   {URIPattern} "org.sindice.siren.qparser.tree.NodeValue":up
                  | {Literal} "org.sindice.siren.qparser.tree.NodeValue":l
                  | {LiteralPattern} "org.sindice.siren.qparser.tree.NodeValue":lp

Operator      ::= enum AND, OR, MINUS