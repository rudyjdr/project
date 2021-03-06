package modules

import com.google.inject.AbstractModule
import dao.{ AuthTokenDAO, AuthTokenDAOImpl }
import net.codingwell.scalaguice.ScalaModule
import services.{ AuthTokenService, AuthTokenServiceImpl }

/**
 * The base Guice module.
 */
class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[AuthTokenDAO].to[AuthTokenDAOImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
  }
}
