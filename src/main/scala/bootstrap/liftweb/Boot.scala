package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._
import mapper._

import code.model._

import http._
import provider._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = 
	new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
			     Props.get("db.url") openOr 
			     "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
			     Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    Schemifier.schemify(true, Schemifier.infoF _, User)

    // where to search snippet
    LiftRules.addToPackages("code")

    // Build SiteMap
    val entries = List(
      Menu.i("Home") / "index", // the simple way to declare a menu

      // more complex because this menu allows anything in the
      // /static path to be visible
      Menu(Loc("Static", Link(List("static"), true, "/static/index"), 
	       "Static Content")),
      Menu(Loc("InvoiceZero", Link(List("invoicezero"), true, "/invoicezero/index"),
	       "GWT Example"))) :::
    // the User management menu items
    User.sitemap

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(SiteMap(entries:_*))

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    
    LiftRules.useXhtmlMimeType = false

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)
    
    //LiftRules.statelessDispatchTable.prepend {
    //    case r @ Req( "invoicezero" :: Nil, _, GetRequest) => ObjJServer.serve(r)
        //case r @ Req( _, ".cache.html", GetRequest) => ObjJServer.serve(r)
        //case r @ Req( _, ".cache.png", GetRequest) => ObjJServer.serve(r)
        //case r @ Req( _, ".gwt.rpc", GetRequest) => ObjJServer.serve(r)
    //  }
    
    }

    /**
     * Force the request to be UTF-8
     */
    private def makeUtf8(req: HTTPRequest) {
      req.setCharacterEncoding("UTF-8")
    }
    
    
  }
  
  object ObjJServer {
    def serve(req: Req)(): Box[LiftResponse] =
    for {
      url <- LiftRules.getResource(req.path.wholePath.mkString("/", "/", ""))
      urlConn <- tryo(url.openConnection)
      lastModified = ResourceServer.calcLastModified(url)
    } yield {
        val stream = url.openStream
        StreamingResponse(stream, () => stream.close, urlConn.getContentLength,
                          (if (lastModified == 0L) Nil else
                           List(("Last-Modified", toInternetDate(lastModified)))) :::
                           List(("Expires", toInternetDate(millis + 30.days)),
                               ("Content-Type","application/text")), Nil, 200)
    }
  
  
}
