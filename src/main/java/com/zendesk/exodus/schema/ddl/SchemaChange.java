package com.zendesk.exodus.schema.ddl;

import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.zendesk.exodus.schema.Database;
import com.zendesk.exodus.schema.Schema;
import com.zendesk.exodus.schema.Table;

public abstract class SchemaChange {
	abstract Schema apply(Schema originalSchema) throws SchemaSyncError;

	protected Database findDatabase(Schema schema, String dbName) throws SchemaSyncError {
		Database database = schema.findDatabase(dbName);

		if ( database == null )
			throw new SchemaSyncError("Couldn't find database " + dbName);

		return database;
	}

	protected Table findTable(Database d, String tableName) throws SchemaSyncError {
		Table table = d.findTable(tableName);

		if ( table == null )
			throw new SchemaSyncError("Couldn't find table " + d.getName() + "." + tableName);
		return table;
	}

	protected Table findTable(Schema schema, String dbName, String tableName) throws SchemaSyncError {
		Database d = findDatabase(schema, dbName);
		return findTable(d, tableName);
	}


	static List<SchemaChange> parse(String currentDB, String sql) {
		ANTLRInputStream input = new ANTLRInputStream(sql);
		mysqlLexer lexer = new mysqlLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		mysqlParser parser = new mysqlParser(tokens);

		ExodusMysqlParserListener listener = new ExodusMysqlParserListener(currentDB);

		System.out.println("Running parse on " + sql);
		ParseTree tree = parser.parse();

		ParseTreeWalker.DEFAULT.walk(listener, tree);
		System.out.println(tree.toStringTree(parser));

		return listener.getSchemaChanges();
	}

}