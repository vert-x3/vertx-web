import java.io.File;

public class Migration {
  public static void main(String[] args) {
    gg.jte.migrate.MigrateV1To2.migrateTemplates(
      new File("/home/paulo/Projects/vert-x3/vertx-web/vertx-template-engines/vertx-web-templ-jte/src/test/jte").toPath()
    );
  }
}
