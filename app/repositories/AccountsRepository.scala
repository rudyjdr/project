package repositories

import javax.inject.{ Inject, Singleton }
import models.Account
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

/**
 * A repository for accounts.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class AccountsRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(
  implicit
  ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[PostgresProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import profile.api._

  /**
   * Here we define the table. It will have a name of people
   */
  private class AccountTable(tag: Tag) extends Table[Account](tag, "account") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def email: Rep[String] = column[String]("email", O.Unique)

    def password: Rep[String] = column[String]("password")

    def category: Rep[String] = column[String]("category")

    def city: Rep[String] = column[String]("city")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */
    def * =
      (id, email, password, category, city) <> ((Account.apply _).tupled, Account.unapply)
  }

  /**
   * The starting point for all queries on the people table.
   */
  private val accounts = TableQuery[AccountTable]

  /**
   * Create a person with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def create(
    email: String,
    password: String,
    category: String,
    city: String): Future[Account] = db.run {
    // We create a projection of just the email, pwd etc columns, since we're not inserting a value for the id column
    (accounts.map(p => (p.email, p.password, p.category, p.city))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning accounts.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((a, id) => Account(id, a._1, a._2, a._3, a._4))
    // And finally, insert the person into the database
    ) += (email, password, category, city)
  }

  def updateEmail(id: Long, email: String) = db.run {
    val q = for { a <- accounts if a.id === id } yield a.email
    q.update(email)
  }

  def deleteAccount(id: Long) = db.run {
    val q = accounts.filter(_.id === id)
    q.delete
  }

  def getAccountById(id: Long): Future[Option[Account]] = db.run {
    val q = accounts.filter(_.id === id)
    q.result.headOption
  }

  /**
   * List all the accounts in the database.
   */
  def list(): Future[Seq[Account]] = db.run {
    accounts.result
  }
}
