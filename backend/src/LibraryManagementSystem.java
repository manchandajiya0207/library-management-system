import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;

public class LibraryManagementSystem {
    private static final Librarian LIBRARY = new Librarian();
    private static final Path FRONTEND = Paths.get("frontend").toAbsolutePath().normalize();

    public static void main(String[] args) throws Exception {
        seed();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/api/", LibraryManagementSystem::handleApi);
        server.createContext("/", LibraryManagementSystem::serveFile);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Library Management System is running at http://localhost:8080");
        System.out.println("Keep this terminal open. Press Ctrl+C to stop.");
        try { java.awt.Desktop.getDesktop().browse(new URI("http://localhost:8080")); } catch (Exception ignored) {}
    }

    private static void seed() throws LibraryException {
        LIBRARY.addBook(new Book(101,"Effective Java","Joshua Bloch"));
        LIBRARY.addBook(new Book(102,"Clean Code","Robert C. Martin"));
        LIBRARY.addBook(new Book(103,"Head First Java","Kathy Sierra"));
        LIBRARY.registerMember(new StudentMember(201,"Aarav Sharma","aarav@example.com"));
        LIBRARY.registerMember(new FacultyMember(301,"Dr. Meera Singh","meera@example.com"));
    }

    private static void handleApi(HttpExchange ex) throws IOException {
        addCors(ex);
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) { ex.sendResponseHeaders(204,-1); return; }
        String path=ex.getRequestURI().getPath();
        try {
            if ("/api/data".equals(path) && "GET".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJson(ex,200,dataJson()); return;
            }
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { sendJson(ex,405,"{\"error\":\"Method not allowed\"}"); return; }
            Map<String,String> p=parseForm(new String(ex.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
            switch(path) {
                case "/api/books": {
                    Book b=new Book(number(p,"id"),required(p,"title"),required(p,"author"));
                    LIBRARY.addBook(b); sendJson(ex,200,"{\"message\":\"Book added\"}"); return;
                }
                case "/api/members": {
                    int id=number(p,"id"); String name=required(p,"name"), email=required(p,"email");
                    String type=required(p,"type");
                    Member m="Faculty".equalsIgnoreCase(type)?new FacultyMember(id,name,email):new StudentMember(id,name,email);
                    LIBRARY.registerMember(m);sendJson(ex,200,"{\"message\":\"Member registered\"}");return;
                }
                case "/api/issue": {
                    int bid=number(p,"bookId"),mid=number(p,"memberId");LIBRARY.issueBook(bid,mid);
                    Transaction t=LIBRARY.getTransactions().get(LIBRARY.getTransactions().size()-1);
                    sendJson(ex,200,"{\"message\":\"Issued\",\"dueDate\":\""+t.getDueDate()+"\"}");return;
                }
                case "/api/return": {
                    double fine=LIBRARY.returnBook(number(p,"bookId"),number(p,"memberId"));
                    sendJson(ex,200,"{\"message\":\"Returned\",\"fine\":"+fine+"}");return;
                }
                default: sendJson(ex,404,"{\"error\":\"Unknown API endpoint\"}");
            }
        } catch (LibraryException | IllegalArgumentException | IllegalStateException err) {
            sendJson(ex,400,"{\"error\":\""+escape(err.getMessage()==null?"Invalid request":err.getMessage())+"\"}");
        } catch (Exception err) {
            sendJson(ex,500,"{\"error\":\"Server error: "+escape(err.getMessage()==null?"unknown":err.getMessage())+"\"}");
        }
    }

    private static void serveFile(HttpExchange ex) throws IOException {
        String path=ex.getRequestURI().getPath();
        if (path.equals("/")) path="/index.html";
        Path file=FRONTEND.resolve(path.substring(1)).normalize();
        if (!file.startsWith(FRONTEND) || !Files.exists(file) || Files.isDirectory(file)) {
            byte[] b="Not found".getBytes(StandardCharsets.UTF_8);ex.sendResponseHeaders(404,b.length);try(OutputStream os=ex.getResponseBody()){os.write(b);}return;
        }
        String name=file.getFileName().toString();
        String type=name.endsWith(".html")?"text/html; charset=utf-8":name.endsWith(".css")?"text/css; charset=utf-8":name.endsWith(".js")?"application/javascript; charset=utf-8":"text/plain; charset=utf-8";
        ex.getResponseHeaders().set("Content-Type",type);
        byte[] b=Files.readAllBytes(file);ex.sendResponseHeaders(200,b.length);try(OutputStream os=ex.getResponseBody()){os.write(b);}
    }
    private static void addCors(HttpExchange ex){ex.getResponseHeaders().set("Access-Control-Allow-Origin","*");ex.getResponseHeaders().set("Access-Control-Allow-Headers","Content-Type");ex.getResponseHeaders().set("Access-Control-Allow-Methods","GET, POST, OPTIONS");}
    private static void sendJson(HttpExchange ex,int status,String json)throws IOException{ex.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");byte[] b=json.getBytes(StandardCharsets.UTF_8);ex.sendResponseHeaders(status,b.length);try(OutputStream os=ex.getResponseBody()){os.write(b);}}
    private static String dataJson(){
        StringBuilder s=new StringBuilder("{\"books\":[");List<Book> books=LIBRARY.getBooks();
        for(int i=0;i<books.size();i++){Book b=books.get(i);if(i>0)s.append(',');s.append("{\"id\":").append(b.getId()).append(",\"title\":\"").append(escape(b.getTitle())).append("\",\"author\":\"").append(escape(b.getAuthor())).append("\",\"available\":").append(b.isAvailable()).append('}');}
        s.append("],\"members\":[");List<Member> members=LIBRARY.getMembers();
        for(int i=0;i<members.size();i++){Member m=members.get(i);if(i>0)s.append(',');s.append("{\"id\":").append(m.getId()).append(",\"name\":\"").append(escape(m.getName())).append("\",\"email\":\"").append(escape(m.getEmail())).append("\",\"type\":\"").append(m.getType()).append("\",\"limit\":").append(m.getBorrowingLimit()).append(",\"loanDays\":").append(m.getLoanPeriodDays()).append('}');}
        s.append("],\"transactions\":[");List<Transaction> ts=LIBRARY.getTransactions();
        for(int i=0;i<ts.size();i++){Transaction t=ts.get(i);if(i>0)s.append(',');s.append("{\"id\":").append(t.getId()).append(",\"bookId\":").append(t.getBook().getId()).append(",\"bookTitle\":\"").append(escape(t.getBook().getTitle())).append("\",\"memberId\":").append(t.getMember().getId()).append(",\"memberName\":\"").append(escape(t.getMember().getName())).append("\",\"issueDate\":\"").append(t.getIssueDate()).append("\",\"dueDate\":\"").append(t.getDueDate()).append("\",\"returnDate\":").append(t.getReturnDate()==null?"null":"\""+t.getReturnDate()+"\"").append(",\"fine\":").append(t.getFine()).append(",\"status\":\"").append(t.isActive()?"Active":"Closed").append("\"}");}
        return s.append("]}").toString();
    }
    private static Map<String,String> parseForm(String body){Map<String,String> m=new HashMap<>();for(String pair:body.split("&")){String[] kv=pair.split("=",2);if(kv.length>0)m.put(decode(kv[0]),kv.length>1?decode(kv[1]):"");}return m;}
    private static String decode(String s){try{return URLDecoder.decode(s,StandardCharsets.UTF_8);}catch(Exception e){return s;}}
    private static String required(Map<String,String> p,String key){String v=p.get(key);if(v==null||v.isBlank())throw new IllegalArgumentException(key+" is required.");return v.trim();}
    private static int number(Map<String,String> p,String key){try{int n=Integer.parseInt(required(p,key));if(n<=0)throw new NumberFormatException();return n;}catch(Exception e){throw new IllegalArgumentException(key+" must be a positive whole number.");}}
    private static String escape(String s){return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n"," ").replace("\r"," ");}
}
