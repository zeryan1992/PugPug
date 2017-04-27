import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.rowset.serial.SerialBlob;
import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zeryan on 1/25/16.
 */
public class Signup extends HttpServlet {
    private Connection connection;

    private PreparedStatement insertStatement;
    private PreparedStatement user_id_prepared;
    private PreparedStatement checkDeviceID;
    private PreparedStatement updateLocation;
    private PreparedStatement checkLogInCred;
    private PreparedStatement checkDeviceIDAndUserID;
    private PreparedStatement  searchPosts;
    private PreparedStatement postMessage;
    private PreparedStatement insertLikes;
    private PreparedStatement updateLikesInPosts;
    private PreparedStatement serachPostId;


    private String mainID="_id";

    private String actionPost="post";
    private String actionCheck="check";
    private String actionLogIn="login";
    private String actionSignup="sign_up";
    private String actionRetrieve="retrieve";
    private String action="action";

    private String check_response="check_response";
    private String checkResponseSuccess="success";
    private String checkResponseFail="fail";

    private HttpServletRequest insideRequest;

    private double lat;
    private double log;


    private UUID id;
    private PreparedStatement searchForlikemethod;
    private PreparedStatement getLikesToUpdateFromPosts;
    private PreparedStatement getAllLikedByUser;
    private PreparedStatement searchReplyid;
    private PreparedStatement insertReply;
    private PreparedStatement updatelikeNuminLikes;
    private PreparedStatement getReplies;
    private PreparedStatement searchForLikedReply;
    private PreparedStatement insertLikesReply;
    private PreparedStatement getIndividualReplyToUpdate;
    private PreparedStatement updateIndividualReplyLikeNum;
    private PreparedStatement updateLikeNuminReplyLikes;
    private PreparedStatement searchForALLLikedReply;
    private PreparedStatement getrepliesCountForPosts;
    private PreparedStatement getPostAfterInsert;
    private String postTemp;
    private String deviceTemp;
    private String user_id;
    private PreparedStatement globalGetPost;
    private PreparedStatement getPostsByUser;
    private PreparedStatement getUsersReplies;
    private PreparedStatement getAllPosts;
    private PreparedStatement updateUserId;
    private PreparedStatement getProPostsByUser;


    @Override
    public void init() throws ServletException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("", "", "");
            connection.createStatement().execute("SET NAMES utf8mb4");

            insertStatement = connection.prepareStatement("INSERT INTO users VALUES(?,?,?,?,?)");
            user_id_prepared=connection.prepareStatement("SELECT user_id FROM users WHERE user_id=?");
            updateLocation=connection.prepareStatement("UPDATE users SET long_v=?, lati_v=?, ip_address=? WHERE device_id=? AND user_id=?");

            checkLogInCred=connection.prepareStatement("SELECT user_id, device_id FROM users WHERE user_id=? AND device_id=?");
            checkDeviceIDAndUserID=connection.prepareStatement("SELECT user_id, device_id FROM users WHERE user_id=? OR device_id=?");
            checkDeviceID=connection.prepareStatement("SELECT device_id FROM users WHERE device_id=?");

            searchPosts=connection.prepareStatement("SELECT * from posts order by post_date DESC LIMIT 50");

            postMessage=connection.prepareStatement("INSERT INTO posts VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");

            insertLikes=connection.prepareStatement("INSERT INTO post_likes VALUES (?,?,?,?,?,?,?,?)");

            searchForlikemethod=connection.prepareStatement("SELECT device_id, post_id, like_num FROM post_likes WHERE device_id=? AND post_id=?");
            updatelikeNuminLikes=connection.prepareStatement("UPDATE post_likes SET like_num=? WHERE post_id=? AND device_id=?");

            getAllLikedByUser=connection.prepareStatement("SELECT post_id, device_id,like_num FROM post_likes WHERE user_id=? AND device_id=?");

            serachPostId=connection.prepareStatement("SELECT post_id FROM posts WHERE post_id=?");

            getLikesToUpdateFromPosts=connection.prepareStatement("SELECT likes FROM posts WHERE post_id=?");
            updateLikesInPosts=connection.prepareStatement("UPDATE posts SET likes=? WHERE post_id=?");

            searchReplyid=connection.prepareStatement("SELECT reply_id FROM replies WHERE reply_id=?");
            insertReply=connection.prepareStatement("INSERT INTO replies VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            getReplies=connection.prepareStatement("SELECT * FROM replies WHERE post_id=? AND is_deleted=? ORDER BY post_date");

            searchForLikedReply=connection.prepareStatement("SELECT * FROM reply_likes WHERE device_id=? and post_id=? AND user_id=? AND reply_id=?");
            insertLikesReply=connection.prepareStatement("INSERT INTO reply_likes VALUES (?,?,?,?,?,?,?,?,?)");

            getIndividualReplyToUpdate=connection.prepareStatement("SELECT * FROM replies WHERE post_id=? AND reply_id=?");
            updateIndividualReplyLikeNum=connection.prepareStatement("UPDATE replies SET likes=? WHERE post_id=? AND reply_id=?");
            updateLikeNuminReplyLikes=connection.prepareStatement("UPDATE reply_likes SET like_num=? WHERE device_id=? AND post_id=? AND reply_id=?");
            searchForALLLikedReply=connection.prepareStatement("SELECT * FROM reply_likes WHERE user_id=? AND device_id=?");




            getrepliesCountForPosts=connection.prepareStatement("select count(*)  from  replies where post_id=? AND is_deleted=?");

            getPostAfterInsert=connection.prepareStatement("SELECT * from posts WHERE post_id=? AND device_id=? AND user_id=? ");

            globalGetPost=connection.prepareStatement("SELECT * from posts WHERE is_public=? and likes>=5 order by post_date DESC LIMIT 50");
            getPostsByUser=connection.prepareStatement("SELECT * FROM posts where device_id=?");

            getUsersReplies=connection.prepareStatement("SELECT * FROM replies WHERE device_id=?");

            getAllPosts=connection.prepareStatement("SELECT * FROM posts ORDER BY post_date DESC");



            updateUserId=connection.prepareStatement("UPDATE users SET user_id=? WHERE device_id=?");










        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setCharacterEncoding("UTF-8");
        Reader mainReader=request.getReader();
        PrintWriter mainPrintWriter=response.getWriter();
        BufferedWriter bufMainWriter=new BufferedWriter(mainPrintWriter);
        BufferedReader bufMainReader=new BufferedReader(mainReader);
        String line=null;
        StringBuffer stringBufferResponse=new StringBuffer();
        JSONObject mainResponseJSON=new JSONObject();
        while ((line=bufMainReader.readLine())!=null)
        {
            stringBufferResponse.append(line);
        }
        bufMainReader.close();

        JSONObject toDoJson=new JSONObject(stringBufferResponse.toString());
        if (!toDoJson.get(action).equals(""))
        {
            this.insideRequest=request;
            if (toDoJson.get(action).equals(actionCheck))
            {
                try {
                    if (checkID(toDoJson))
                    {
                        mainResponseJSON.put(check_response,checkResponseSuccess);
                        mainResponseJSON.put(mainID,id.toString());
                        bufMainWriter.write(mainResponseJSON.toString());
                        bufMainWriter.flush();
                        bufMainWriter.close();
                        mainPrintWriter.close();
                    }
                    else
                    {
                        mainResponseJSON.put(check_response,checkResponseFail);
                        bufMainWriter.write(mainResponseJSON.toString());
                        bufMainWriter.flush();
                        bufMainWriter.close();
                        mainPrintWriter.close();


                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else if (toDoJson.get(action).equals(actionSignup))
            {
                try {

                    if (signUserUp(toDoJson)) {
                        mainResponseJSON.put("sign_res", checkResponseSuccess);
                        bufMainWriter.write(mainResponseJSON.toString());
                        bufMainWriter.flush();
                        bufMainWriter.close();
                        mainPrintWriter.close();


                    } else {
                        bufMainWriter.write("Something horribly went wrong");
                        bufMainWriter.flush();
                        bufMainWriter.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            else if (toDoJson.get(action).equals(actionLogIn))
            {
                try {

                    if (logIn(toDoJson))
                    {
                        mainResponseJSON.put("login_res",checkResponseSuccess);
                        bufMainWriter.write(mainResponseJSON.toString());
                        bufMainWriter.flush();
                        bufMainWriter.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else if (toDoJson.getString(action).equals(actionRetrieve))
            {
                try {
                    if (logIn(toDoJson))
                    {
                        mainResponseJSON.put("retrieve_res",checkResponseSuccess);
                        mainResponseJSON.put("posts",retrieve(toDoJson));
                        bufMainWriter.write(mainResponseJSON.toString());
                        bufMainWriter.flush();
                        bufMainWriter.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else if (toDoJson.getString(action).equals(actionPost))
            {
                try {
                    if(logIn(toDoJson))
                    {

                        if (postMessage(toDoJson)) {

                            JSONObject post=getOnePost(postTemp,deviceTemp,user_id);
                            mainResponseJSON.put("post_res", checkResponseSuccess);
                            mainResponseJSON.put("post",post);
                            bufMainWriter.write(mainResponseJSON.toString());
                            bufMainWriter.flush();
                            bufMainWriter.close();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            else if (toDoJson.getString(action).equals("like"))
            {
                try {

                    if (logIn(toDoJson))
                    {
                        if (insertLike(toDoJson)) {
                            if (updateLikes(toDoJson))
                            {
                                mainResponseJSON.put("like_res", checkResponseSuccess);
                                bufMainWriter.write(mainResponseJSON.toString());
                                bufMainWriter.flush();
                                bufMainWriter.close();
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else if (toDoJson.getString(action).equals("post_reply"))
            {
                try {
                    if (logIn(toDoJson))
                    {
                        JSONObject reply=postAreply(toDoJson,request);
                        mainResponseJSON.put("reply_res",checkResponseSuccess);
                        mainResponseJSON.put("reply",reply);
                        bufMainWriter.write(mainResponseJSON.toString());
                        bufMainWriter.flush();
                        bufMainWriter.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else if (toDoJson.getString(action).equals("retrieve_replies"))
            {
                try {
                    if (logIn(toDoJson))
                    {
                        mainResponseJSON.put("retrieve_res",checkResponseSuccess);
                        mainResponseJSON.put("replies",getReplies(toDoJson));
                        bufMainWriter.write(mainResponseJSON.toString());
                        bufMainWriter.flush();
                        bufMainWriter.close();

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else if (toDoJson.get(action).equals("like_reply"))
            {
                try
                {
                    if (logIn(toDoJson))
                    {
                        if (insertLikeReply(toDoJson))
                        {
                            if (updateLikesInReplies(toDoJson))
                            {
                                mainResponseJSON.put("like_res", checkResponseSuccess);
                                bufMainWriter.write(mainResponseJSON.toString());
                                bufMainWriter.flush();
                                bufMainWriter.close();
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else if (toDoJson.get(action).equals("global_retrieve"))
            {
                try {
                    if (logIn(toDoJson))
                    {
                        mainResponseJSON.put("retrieve_glob",checkResponseSuccess);
                        mainResponseJSON.put("posts",getPublic(toDoJson));
                        bufMainWriter.write(mainResponseJSON.toString());
                        bufMainWriter.flush();
                        bufMainWriter.close();
                    }
                    else
                    {
                        mainResponseJSON.put("retrieve_glob",checkResponseFail);
                        bufMainWriter.write(mainResponseJSON.toString());
                        bufMainWriter.flush();
                        bufMainWriter.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else if (toDoJson.get(action).equals("my_posts"))
            {
                try {
                    if (logIn(toDoJson))
                    {
                        mainResponseJSON.put("my_res",checkResponseSuccess);
                        mainResponseJSON.put("posts",getUsersPosts(toDoJson));
                        bufMainWriter.write(mainResponseJSON.toString());
                        bufMainWriter.flush();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else if (toDoJson.get(action).equals("my_replies"))
            {
                try {
                    if (logIn(toDoJson))
                    {
                        mainResponseJSON.put("my_res",checkResponseSuccess);
                        mainResponseJSON.put("posts",userReplies(toDoJson));

                        bufMainWriter.write(mainResponseJSON.toString());
                        bufMainWriter.flush();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else if (toDoJson.get(action).equals("report"))
            {
                try {
                    if (logIn(toDoJson))
                    {
                        if (reportInsertPost(toDoJson)) {
                            mainResponseJSON.put("report_res", checkResponseSuccess);
                            bufMainWriter.write(mainResponseJSON.toString());
                            bufMainWriter.flush();
                            bufMainWriter.close();

                        }

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else if (toDoJson.get(action).equals("delete"))
            {
                try {
                    if (logIn(toDoJson))
                    {
                        if(deletePost(toDoJson))
                        {
                            mainResponseJSON.put("delete_res", checkResponseSuccess);
                            bufMainWriter.write(mainResponseJSON.toString());
                            bufMainWriter.flush();
                            bufMainWriter.close();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else if (toDoJson.get(action).equals("delete_reply"))
            {
                try {
                    if (logIn(toDoJson))
                    {
                        if(deleteReply(toDoJson))
                        {
                            mainResponseJSON.put("delete_res", checkResponseSuccess);
                            bufMainWriter.write(mainResponseJSON.toString());
                            bufMainWriter.flush();
                            bufMainWriter.close();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public boolean deletePost(JSONObject toDoJson) throws SQLException {
        String d_id=toDoJson.getString("app_id");
        String post_id=toDoJson.getString("post_id");
        String user_id=toDoJson.getString("user_id");
        PreparedStatement delete=connection.prepareStatement("UPDATE posts SET is_deleted=? WHERE user_id=? AND device_id=? AND post_id=?");
        delete.clearParameters();
        delete.setBoolean(1,true);
        delete.setString(2,user_id);
        delete.setString(3,d_id);
        delete.setString(4,post_id);
        delete.execute();
        delete.close();
        return true;

    }
    public boolean deleteReply(JSONObject toDoJson) throws SQLException {
        String d_id=toDoJson.getString("app_id");
        String post_id=toDoJson.getString("post_id");
        String user_id=toDoJson.getString("user_id");
        String reply_id=toDoJson.getString("reply_id");
        PreparedStatement delete=connection.prepareStatement("UPDATE replies SET is_deleted=? WHERE user_id=? AND device_id=? AND post_id=? AND reply_id=?");
        delete.clearParameters();
        delete.setBoolean(1,true);
        delete.setString(2,user_id);
        delete.setString(3,d_id);
        delete.setString(4,post_id);
        delete.setString(5,reply_id);
        delete.execute();
        delete.close();
        return true;

    }

    private boolean reportInsertPost(JSONObject toDoJson) throws SQLException {
        PreparedStatement insertReport=connection.prepareStatement("INSERT INTO report_posts VALUES (?,?,?,?,?)");
        insertReport.clearParameters();
        String post_id=toDoJson.getString("post_id");
        String user_id=toDoJson.getString("user_id");
        String d_id=toDoJson.getString("app_id");
        String reportType=toDoJson.getString("type");
        String danegr=toDoJson.getString("level");
        insertReport.setString(1,user_id);
        insertReport.setString(2,d_id);
        insertReport.setString(3,post_id);
        insertReport.setString(4,reportType);
        insertReport.setString(5,danegr);
        insertReport.execute();
        return true;
    }

    private boolean updateLikesInReplies(JSONObject toDoJson) throws SQLException {
        String post_id=toDoJson.getString("post_id");
        String reply_id=toDoJson.getString("reply_id");
        int like=toDoJson.getInt("like");
        getIndividualReplyToUpdate.clearParameters();
        getIndividualReplyToUpdate.setString(1,post_id);
        getIndividualReplyToUpdate.setString(2,reply_id);
        ResultSet search=getIndividualReplyToUpdate.executeQuery();
        if (search.next())
        {
            if (like==1) {
                int oldLike = search.getInt("likes");
                int newLike = oldLike + 1;
                updateIndividualReplyLikeNum.clearParameters();
                updateIndividualReplyLikeNum.setInt(1,newLike);
                updateIndividualReplyLikeNum.setString(2,post_id);
                updateIndividualReplyLikeNum.setString(3,reply_id);
                updateIndividualReplyLikeNum.execute();
                search.close();
                return true;

            }
            else if (like==-1)
            {
                int oldLike = search.getInt("likes");
                int newLike = oldLike - 1;
                updateIndividualReplyLikeNum.clearParameters();
                updateIndividualReplyLikeNum.setInt(1,newLike);
                updateIndividualReplyLikeNum.setString(2,post_id);
                updateIndividualReplyLikeNum.setString(3,reply_id);
                updateIndividualReplyLikeNum.execute();
                search.close();
                return true;

            }


        }
        search.close();
        return false;
    }


    private boolean insertLikeReply(JSONObject toDoJson) throws SQLException {
        String post_id=toDoJson.getString("post_id");
        String reply_id=toDoJson.getString("reply_id");
        String device_id=toDoJson.getString("app_id");
        String user_id=toDoJson.getString("user_id");
        int like=toDoJson.getInt("like");
        if (like==1||like==-1)
        {
            insertLikesReply.clearParameters();
            insertLikesReply.setString(1,user_id);
            insertLikesReply.setString(2,device_id);
            insertLikesReply.setString(3,post_id);
            insertLikesReply.setString(4,reply_id);
            insertLikesReply.setInt(5,like);
            insertLikesReply.setDouble(6,0);
            insertLikesReply.setDouble(7,0);
            insertLikesReply.setString(8,"");
            insertLikesReply.setString(9,null);
            insertLikesReply.execute();
            return true;
        }
        return false;
    }



    public JSONArray getReplies(JSONObject mainJSON) throws SQLException, IOException {
        String d_id=mainJSON.getString("app_id");
        String post_id=mainJSON.getString("post_id");
        String user_id=mainJSON.getString("user_id");
        HashMap<String,Integer> allLikedReplies=getLikedReplies(user_id,d_id);
        PreparedStatement deleted=connection.prepareStatement("SELECT is_deleted FROM posts WHERE post_id=? AND is_deleted=?");
        PreparedStatement blockedPosts=connection.prepareStatement("SELECT * FROM report_posts WHERE user_id=? AND device_id=? AND post_or_reply_id=?");
        deleted.clearParameters();
        deleted.setString(1,post_id);
        deleted.setBoolean(2,false);
        ResultSet is_deleted=deleted.executeQuery();
        getReplies.clearParameters();
        getReplies.setString(1,post_id);
        getReplies.setBoolean(2,false);
        ResultSet searchResults=getReplies.executeQuery();
        JSONArray jsonArray=new JSONArray();
        if (is_deleted.next()) {
            if (!is_deleted.getBoolean("is_deleted")) {
                while (searchResults.next()) {
                    blockedPosts.clearParameters();
                    blockedPosts.setString(1,user_id);
                    blockedPosts.setString(2,d_id);
                    blockedPosts.setString(3,searchResults.getNString("reply_id"));
                    ResultSet set=blockedPosts.executeQuery();
                    if (!set.next()) {
                        JSONObject reply_res = new JSONObject();
                        String postId = searchResults.getNString("post_id");
                        if (postId.equals(post_id)) {
                            String replyid = searchResults.getNString("reply_id");
                            int reply_likes = searchResults.getInt("likes");
                            String clear_text = searchResults.getNString("clear_text");
                            String icon = searchResults.getString("icon");
                            Date post_date = searchResults.getDate("post_date");
                            Time post_time = searchResults.getTime("post_date");
                            String image = getImage("" + icon);
                            if (searchResults.getNString("user_id").equals(user_id) && searchResults.getNString("device_id").equals(d_id)) {
                                reply_res.put("self", true);
                            } else {
                                reply_res.put("self", false);
                            }
                            reply_res.put("post_id", postId);
                            reply_res.put("reply_id", replyid);
                            reply_res.put("likes", reply_likes);
                            reply_res.put("text", clear_text);
                            reply_res.put("icon", image);
                            reply_res.put("post_date", post_date.toString());
                            reply_res.put("post_time", post_time.toString());
                            if (allLikedReplies.containsKey(replyid)) {
                                reply_res.put("like_res", allLikedReplies.get(replyid));
                            }
                            jsonArray.put(reply_res);
                            set.close();
                        }
                    }
                }
            }
        }
        is_deleted.close();
        searchResults.close();
        return jsonArray;
    }

    private boolean postMessage(JSONObject mainJSON) throws SQLException, IOException, ParseException {
        String user_idLo = mainJSON.getString("user_id");
        String dID=mainJSON.getString("app_id");
        double lat=mainJSON.getDouble("lat");
        double log=mainJSON.getDouble("log");
        String message=mainJSON.getString("text");
        String ip=insideRequest.getRemoteAddr();
        String dateJ=mainJSON.getString("date");
        String time=mainJSON.getString("time");
        StringBuffer buf=new StringBuffer();
        buf.append(dateJ+" "+time);
        String[] address=getAddress(String.valueOf(lat),String.valueOf(log));
        String post_id=getpostID();
        postTemp=post_id;
        deviceTemp=dID;
        this.user_id=user_idLo;
        postMessage.clearParameters();
        postMessage.setString(1,user_idLo);
        postMessage.setString(2,dID);
        postMessage.setString(3,post_id);
        postMessage.setDouble(4,lat);
        postMessage.setDouble(5,log);
        postMessage.setString(6,ip);
        postMessage.setInt(7,0);
        postMessage.setString(8,message);
        postMessage.setString(9,buf.toString());
        postMessage.setString(10,address[1]);
        postMessage.setString(11,address[3]);


        if (!mainJSON.isNull("is_public"))
        {
            if (mainJSON.getBoolean("is_public"))
            {
                postMessage.setBoolean(12,mainJSON.getBoolean("is_public"));
            }
            else
            {
                postMessage.setBoolean(12,mainJSON.getBoolean("is_public"));
            }
        }
        postMessage.setString(13,address[0]);
        postMessage.setInt(14,0);
        postMessage.setBoolean(15,false);
        boolean bo=mainJSON.getBoolean("is_image");
        Blob blob=null;
        if (!mainJSON.isNull("is_image")&&mainJSON.getBoolean("is_image"))
        {
            String image=mainJSON.getString("img");
            postMessage.setBoolean(16,bo);
            byte[] bytes=saveImage(dID,post_id,image);
            blob=new SerialBlob(bytes);
            blob.setBytes(1,bytes);
            postMessage.setBlob(17,blob);
        }
        else
        {
            postMessage.setBoolean(16,bo);
            postMessage.setBlob(17,blob);
        }
        postMessage.execute();
        return true;

    }

    private byte[] saveImage(String dID, String post_id, String image) throws IOException {
        //File file=new File("/Users/zeryan/Desktop/h/"+dID+"/"+post_id+".jpg");
        BASE64Decoder decode=new BASE64Decoder();
        byte[] bytes= decode.decodeBuffer(image);
        return bytes;
    }

    public  String getpostID() throws SQLException {
        boolean checker=false;
        Random random = new Random();
        StringBuffer buffer = null;
        while (!checker) {
            buffer = new StringBuffer();
            for (int i = 9; i < 20; i++) {
                char r = (char) (random.nextInt(20) + 95);
                buffer.append(r);
            }
            serachPostId.setString(1,buffer.toString());
            ResultSet resultSet=serachPostId.executeQuery();
            if (!resultSet.next())
            {
                checker=true;
                resultSet.close();
            }
        }
        return buffer.toString();

    }
    public boolean insertLike(JSONObject mainJSON) throws SQLException {
        String d_id=mainJSON.getString("app_id");
        String user_id=mainJSON.getString("user_id");
        String post_id=mainJSON.getString("post_id");
        int like=mainJSON.getInt("like");
        if (like==1||like==-1) {
            insertLikes.setString(1,user_id );
            insertLikes.setString(2,d_id);
            insertLikes.setString(3, post_id);
            insertLikes.setInt(4, like);
            insertLikes.setDouble(5,0);
            insertLikes.setDouble(6,0);
            insertLikes.setString(7,"");
            insertLikes.setString(8,null);
            insertLikes.execute();
            return true;
        }

        return false;
    }
    public boolean updateLikes(JSONObject mainJSON) throws SQLException {
        String post_id=mainJSON.getString("post_id");
        int like=mainJSON.getInt("like");
        getLikesToUpdateFromPosts.setString(1,post_id);
        ResultSet resultSet=getLikesToUpdateFromPosts.executeQuery();
        if (resultSet.next())
        {
            int likes=resultSet.getInt("likes");
            if (like==1) {
                int newLike=likes+1;
                updateLikesInPosts.setInt(1, newLike);
                updateLikesInPosts.setString(2, post_id);
                updateLikesInPosts.execute();
                resultSet.close();
                return true;
            }
            else if (like==-1)
            {
                int newLike=likes-1;
                updateLikesInPosts.setInt(1, newLike);
                updateLikesInPosts.setString(2, post_id);
                updateLikesInPosts.execute();
                resultSet.close();
                return true;

            }
        }
        resultSet.close();
        return false;
    }



    public boolean signUserUp(JSONObject mainJSON) throws IOException, JSONException, SQLException {
        if (!mainJSON.toString().equals(""))
        {
            insertStatement.clearParameters();
            String user_id = mainJSON.getString("sign_id");
            String dID=mainJSON.getString("app_id");
            checkDeviceIDAndUserID.clearParameters();
            checkDeviceIDAndUserID.setString(1,user_id);
            checkDeviceIDAndUserID.setString(2,dID);
            ResultSet resultSet=checkDeviceIDAndUserID.executeQuery();
            if (!resultSet.next())
            {
                insertStatement.setString(1, user_id);
                insertStatement.setString(2, dID);
                insertStatement.setDouble(3, 0);
                insertStatement.setDouble(4, 0);
                insertStatement.setString(5,"666");
                insertStatement.execute();
                resultSet.close();
                return true;
            }
            else
            {
                checkDeviceID.clearParameters();
                checkDeviceID.setString(1,dID);
                ResultSet set=checkDeviceID.executeQuery();
                if (set.next())
                {
                    if (set.getNString("device_id").equals(dID))
                    {
                        updateUserId.clearParameters();
                        updateUserId.setString(1,user_id);
                        updateUserId.setString(2,dID);
                        updateUserId.execute();
                        resultSet.close();
                        set.close();
                        return true;
                    }
                }
            }
        }
        return false;

    }

    private boolean checkID(JSONObject mainJSON) throws IOException, SQLException
    {
        boolean checker = false;
        if (mainJSON.get(action).equals(actionCheck)) {
            while (!checker) {
                id = UUID.randomUUID();
                user_id_prepared.clearParameters();
                user_id_prepared.setString(1, id.toString());
                ResultSet resSet = user_id_prepared.executeQuery();
                if (!resSet.next()) {
                    checker = true;
                    resSet.close();
                }
            }
        }
        return checker;

    }






    private boolean logIn(JSONObject mainJSON) throws IOException, SQLException {
        String _id= (String) mainJSON.get("user_id");
        String deviceID=mainJSON.getString("app_id");
        lat=mainJSON.getDouble("lat");
        log=mainJSON.getDouble("log");
        String ipLogin=insideRequest.getRemoteAddr();
        checkLogInCred.clearParameters();
        checkLogInCred.setString(1,_id);
        checkLogInCred.setString(2,deviceID);
        ResultSet resSet=checkLogInCred.executeQuery();

        if (resSet.next())
        {
            if (resSet.getNString("user_id").equals(_id) && resSet.getNString("device_id").equals(deviceID)) {
                updateLocation.clearParameters();
                updateLocation.setDouble(1,log);
                updateLocation.setDouble(2,lat);
                updateLocation.setString(3,ipLogin);
                updateLocation.setString(4,deviceID);
                updateLocation.setString(5,_id);
                updateLocation.execute();
                resSet.close();
                return true;
            }
        }
        resSet.close();
        return false;
    }
    public JSONObject getOnePost(String post_id, String device_id, String user_id) throws SQLException {
        getPostAfterInsert.clearParameters();
        getPostAfterInsert.setString(1,post_id);
        getPostAfterInsert.setString(2,device_id);
        getPostAfterInsert.setString(3,user_id);
        ResultSet searchResults=getPostAfterInsert.executeQuery();
        JSONObject jsonObject=new JSONObject();
        while (searchResults.next())
        {
            String postID=searchResults.getNString("post_id");
            String clear_post=searchResults.getNString("clear_text");
            Date date=searchResults.getDate("post_date");
            Time time=searchResults.getTime("post_date");
            int likes=searchResults.getInt("likes");
            jsonObject.put("post_id",postID);
            jsonObject.put("post_date",date);
            jsonObject.put("post_time",time);
            jsonObject.put("text",clear_post);
            jsonObject.put("likes",likes);
            if (searchResults.getInt("reply_count")<108)
            {
                jsonObject.put("can_reply",true);
            }
            else
            {
                jsonObject.put("can_reply",false);
            }
            if (searchResults.getNString("user_id").equals(user_id)&&searchResults.getNString("device_id").equals(device_id))
            {
                jsonObject.put("self",true);
            }
            else
            {
                jsonObject.put("self",false);
            }
            if (searchResults.getBoolean("is_image"))
            {
                String img= null;
                try {
                    img = getMainImage(searchResults.getBlob("image"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                jsonObject.put("img",img);
            }
            getrepliesCountForPosts.clearParameters();
            getrepliesCountForPosts.setString(1,postID);
            getrepliesCountForPosts.setBoolean(2,false);
            ResultSet repliesCount=getrepliesCountForPosts.executeQuery();
            if (repliesCount.next())
            {
                jsonObject.put("reply_count",repliesCount.getInt("count(*)"));
            }
                repliesCount.close();
            }
        searchResults.close();
        return jsonObject;
    }



    public JSONArray retrieve(JSONObject mainJSON) throws SQLException, IOException {
        String deviceID=mainJSON.getString("app_id");
        String _id= (String) mainJSON.get("user_id");
        PreparedStatement blockedPosts=connection.prepareStatement("SELECT * FROM report_posts WHERE user_id=? AND device_id=? AND post_or_reply_id=?");
        HashMap<String,Integer> hash=getLikedPosts(_id,deviceID);
        ResultSet searchResults=searchPosts.executeQuery();
        JSONArray jsonArray=new JSONArray();
        while (searchResults.next())
        {
            if (!searchResults.getBoolean("is_deleted"))
            {
                blockedPosts.clearParameters();
                blockedPosts.setString(1, _id);
                blockedPosts.setString(2, deviceID);
                blockedPosts.setString(3, searchResults.getNString("post_id"));
                ResultSet blockedRes = blockedPosts.executeQuery();
                if (!blockedRes.next())
                {
                    getPostsByUser.clearParameters();
                    getPostsByUser.setString(1, deviceID);
                    ResultSet postsUser = getPostsByUser.executeQuery();
                    double bseLati = searchResults.getDouble("lati_v");
                    double bseLong = searchResults.getDouble("long_v");
                    double distance = distanceCalculatorMiles(lat, log, bseLati, bseLong);
                    if (distance <= 1.2)
                    {
                        String postID = searchResults.getNString("post_id");
                        String clear_post = searchResults.getNString("clear_text");
                        Date date = searchResults.getDate("post_date");
                        Time time = searchResults.getTime("post_date");
                        int likes = searchResults.getInt("likes");
                        String p_Device_id=searchResults.getNString("device_id");
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("post_id", postID);
                        jsonObject.put("post_date", date);
                        jsonObject.put("post_time", time);
                        jsonObject.put("text", clear_post);
                        jsonObject.put("likes", likes);
                        if (searchResults.getBoolean("is_image"))
                        {
                            String img=getMainImage(searchResults.getBlob("image"));
                            jsonObject.put("img",img);

                        }
                        int reply_count = searchResults.getInt("reply_count");
                        if (reply_count < 108)
                        {
                            jsonObject.put("can_reply", true);
                        }
                        else
                        {
                            jsonObject.put("can_reply", false);
                        }

                        while (postsUser.next())
                        {
                            if (postsUser.getNString("post_id").equals(postID)) {
                                jsonObject.put("self", true);
                                break;
                            }
                        }
                        getrepliesCountForPosts.clearParameters();
                        getrepliesCountForPosts.setString(1, postID);
                        getrepliesCountForPosts.setBoolean(2,false);
                        ResultSet repliesCount = getrepliesCountForPosts.executeQuery();
                        if (repliesCount.next())
                        {
                            jsonObject.put("reply_count", repliesCount.getInt("count(*)"));
                        }
                        if (hash.containsKey(postID))
                        {
                            jsonObject.put("like_res", hash.get(postID));
                        }

                        postsUser.close();
                        repliesCount.close();
                        jsonArray.put(jsonObject);
                    }
                    blockedRes.close();
                }
            }
        }
        searchResults.close();
        return jsonArray;
    }
    public String getImage(String num) throws IOException {
        InputStream inS=getServletContext().getResourceAsStream("/new_graphics/" +num+".png");
        byte[] inputStream = IOUtils.toByteArray(inS);


        BASE64Encoder encoder=new BASE64Encoder();
        String f=encoder.encode(inputStream);
        try {
            inS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }
    public JSONObject postAreply(JSONObject mainJSON, HttpServletRequest request) throws SQLException, IOException {
        String deviceID=mainJSON.getString("app_id");
        String user_id=mainJSON.getString("user_id");
        String post_id=mainJSON.getString("post_id");
        String ip_address=request.getRemoteAddr();
        double lat= mainJSON.getDouble("lat");
        double log=mainJSON.getDouble("log");
        String dateJ=mainJSON.getString("date");
        String time=mainJSON.getString("time");
        StringBuffer buf=new StringBuffer();
        buf.append(dateJ+" "+time);
        String reply_id=getReplyID();
        String icon=getAnIcon(mainJSON);
        String image=getImage(icon);
        int likes=0;
        PreparedStatement getCount=connection.prepareStatement("SELECT reply_count FROM posts WHERE post_id=?");
        getCount.clearParameters();
        getCount.setString(1,post_id);
        ResultSet replyCountN=getCount.executeQuery();
        PreparedStatement doesExists=connection.prepareStatement("SELECT * FROM replies WHERE post_id=? AND user_id=? AND device_id=? ");
        doesExists.clearParameters();
        doesExists.setString(1,post_id);
        doesExists.setString(2,user_id);
        doesExists.setString(3,deviceID);
        ResultSet doesEx=doesExists.executeQuery();
        if (doesEx.next())
        {
            String reply_text=mainJSON.getString("reply_text");
            insertReply.clearParameters();
            insertReply.setString(1,user_id);
            insertReply.setString(2,deviceID);
            insertReply.setString(3,post_id);
            insertReply.setString(4,reply_id);
            insertReply.setDouble(5,lat);
            insertReply.setDouble(6,log);
            insertReply.setString(7,ip_address);
            insertReply.setInt(8,likes);
            insertReply.setString(9,reply_text);
            insertReply.setString(10,buf.toString());
            insertReply.setString(11,"");
            insertReply.setString(12,"");
            insertReply.setBoolean(13,false);
            insertReply.setString(14,icon);
            insertReply.setString(15,"");
            insertReply.setBoolean(16,false);
            insertReply.execute();
            PreparedStatement oneReply=connection.prepareStatement("SELECT * FROM replies WHERE post_id=? AND reply_id=? AND user_id=? AND device_id=?");
            oneReply.clearParameters();
            oneReply.setString(1,post_id);
            oneReply.setString(2,reply_id);
            oneReply.setString(3,user_id);
            oneReply.setString(4,deviceID);
            ResultSet set=oneReply.executeQuery();
            if (set.next())
            {
                JSONObject object=new JSONObject();
                object.put("post_id",post_id);
                object.put("reply_id",reply_id);
                object.put("like_res",0);
                object.put("icon",image);
                object.put("text",reply_text);
                object.put("likes",0);
                object.put("post_date",set.getDate("post_date"));
                object.put("post_time",set.getTime("post_date"));
                object.put("self",true);
                doesEx.close();
                replyCountN.close();
                set.close();

                return object;

            }
            set.close();
        }
        else
        {
            PreparedStatement updateCount=connection.prepareStatement("UPDATE posts SET reply_count=? WHERE post_id=?");
            if (replyCountN.next())
            {
                int old=replyCountN.getInt("reply_count");
                int newCount=old+1;
                if (newCount<108)
                {
                    updateCount.clearParameters();
                    updateCount.setInt(1,newCount);
                    updateCount.setString(2,post_id);
                    updateCount.execute();
                    String reply_text=mainJSON.getString("reply_text");
                    insertReply.clearParameters();
                    insertReply.setString(1,user_id);
                    insertReply.setString(2,deviceID);
                    insertReply.setString(3,post_id);
                    insertReply.setString(4,reply_id);
                    insertReply.setDouble(5,lat);
                    insertReply.setDouble(6,log);
                    insertReply.setString(7,ip_address);
                    insertReply.setInt(8,likes);
                    insertReply.setString(9,reply_text);
                    insertReply.setString(10,buf.toString());
                    insertReply.setString(11,"");
                    insertReply.setString(12,"");
                    insertReply.setBoolean(13,false);
                    insertReply.setString(14,icon);
                    insertReply.setString(15,"");
                    insertReply.setBoolean(16,false);
                    insertReply.execute();
                    PreparedStatement oneReply=connection.prepareStatement("SELECT * FROM replies WHERE post_id=? AND reply_id=? AND user_id=? AND device_id=?");
                    oneReply.clearParameters();
                    oneReply.setString(1,post_id);
                    oneReply.setString(2,reply_id);
                    oneReply.setString(3,user_id);
                    oneReply.setString(4,deviceID);
                    ResultSet set=oneReply.executeQuery();
                    if (set.next())
                    {
                        JSONObject object=new JSONObject();

                        object.put("post_id",post_id);
                        object.put("reply_id",reply_id);
                        object.put("like_res",0);
                        object.put("icon",image);
                        object.put("text",reply_text);
                        object.put("likes",0);
                        object.put("post_date",set.getDate("post_date"));
                        object.put("post_time",set.getTime("post_date"));
                        object.put("self",true);
                        doesEx.close();
                        replyCountN.close();
                        set.close();
                        return object;

                    }
                }
            }
        }
        replyCountN.close();
        doesEx.close();
        return null;

    }
    public String getAnIcon(JSONObject mainJSON) throws SQLException {
        String deviceID=mainJSON.getString("app_id");
        String user_id=mainJSON.getString("user_id");
        String post_id=mainJSON.getString("post_id");
        String icon=null;
        PreparedStatement getPost=connection.prepareStatement("SELECT * FROM posts WHERE post_id=? AND device_id=? AND user_id=?");
        getPost.clearParameters();
        getPost.setString(1,post_id);
        getPost.setString(2,deviceID);
        getPost.setString(3,user_id);
        ResultSet op=getPost.executeQuery();
        if (op.next())
        {
            icon="op";
            op.close();
            return icon;
        }
        else
        {
            op.close();
            PreparedStatement getTaken=connection.prepareStatement("SELECT icon FROM replies WHERE post_id=?");
            getTaken.clearParameters();
            getTaken.setString(1,post_id);
            ResultSet takenRes=getTaken.executeQuery();
            int[] icons={1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107};
            Random random=new Random();
            PreparedStatement pr=connection.prepareStatement("SELECT * FROM replies WHERE user_id=? AND device_id=? AND post_id=?");
            pr.clearParameters();
            pr.setString(1,user_id);
            pr.setString(2,deviceID);
            pr.setString(3,post_id);
            ResultSet check=pr.executeQuery();
            if (check.next())
            {
                icon=check.getNString("icon");
                check.close();
                takenRes.close();
                return icon;
            }
            else
            {
                while (takenRes.next())
                {
                    int randIcon = random.nextInt(icons.length) + 1;
                    if (!takenRes.getNString("icon").equals("op")&&icons[randIcon]!=Integer.parseInt(takenRes.getString("icon"))) {
                        icon = "" + randIcon;
                        takenRes.close();
                        check.close();
                        return icon;
                    }
                }
                int randIcon=random.nextInt(icons.length)+1;
                icon=""+randIcon;
                takenRes.close();
                check.close();
                return icon;
            }

        }



    }
    public  String getReplyID() throws SQLException {
        boolean checker=true;
        Random random = new Random();
        StringBuffer buffer = null;
        while (checker) {
            buffer = new StringBuffer();
            for (int i = 9; i < 15; i++) {
                char r = (char) (random.nextInt(20) + 95);
                buffer.append(r);
            }
            searchReplyid.clearParameters();
            searchReplyid.setString(1,buffer.toString());
            ResultSet replyRes=searchReplyid.executeQuery();
            if (!replyRes.next())
            {
                replyRes.close();
                checker=false;
            }
        }
        return buffer.toString();

    }
    public HashMap<String,Integer> getLikedPosts(String user_id,String device_id) throws SQLException {
        getAllLikedByUser.clearParameters();
        getAllLikedByUser.setString(1,user_id);
        getAllLikedByUser.setString(2,device_id);
        ResultSet res=getAllLikedByUser.executeQuery();
        HashMap<String,Integer> hash=new HashMap<>();

        while (res.next())
        {
            hash.put(res.getNString("post_id"),res.getInt("like_num"));
        }
        res.close();
        return hash;

    }
    public HashMap<String,Integer> getLikedReplies(String user_id, String device_id) throws SQLException {
        searchForALLLikedReply.clearParameters();
        searchForALLLikedReply.setString(1,user_id);
        searchForALLLikedReply.setString(2,device_id);
        ResultSet res=searchForALLLikedReply.executeQuery();
        HashMap<String,Integer> hash=new HashMap<>();
        while (res.next())
        {
            hash.put(res.getNString("reply_id"),res.getInt("like_num"));
        }
        res.close();
        return hash;

    }

    public  double distanceCalculatorMiles(double lat1, double lon1, double lat2, double lon2)
    {
        double theta=lon1-lon2;
        double dist = Math.sin(degTorad(lat1)) * Math.sin(degTorad(lat2)) + Math.cos(degTorad(lat1)) * Math.cos(degTorad(lat2)) * Math.cos(degTorad(theta));
        dist=Math.acos(dist);
        dist = radTodeg(dist);
        dist = dist * 60 * 1.1515;
        return dist;

    }
    public String[] getAddress(String lat,String log)
    {
        Geocoder geocoder=new Geocoder();
        GeocoderRequest geocoderRequest=new GeocoderRequest();
        LatLng latLng=new LatLng(lat,log);
        geocoderRequest.setLocation(latLng);
        GeocodeResponse response= null;
        try {
            response = geocoder.geocode(geocoderRequest);

        } catch (IOException e) {
            e.printStackTrace();
        }
        List<GeocoderResult> res=response.getResults();

        GeocoderResult result=res.get(0);

        return result.getFormattedAddress().split(",");

    }
    public  double radTodeg(double rad)
    {
        return (rad*180/Math.PI);
    }
    public  double degTorad(double deg)
    {
        return (deg * Math.PI /180.0);
    }
    public JSONArray getPublic(JSONObject mainJSON) throws SQLException, IOException {
        String deviceID=mainJSON.getString("app_id");
        String user_id=mainJSON.getString("user_id");
        PreparedStatement blockedPosts=connection.prepareStatement("SELECT * FROM report_posts WHERE user_id=? AND device_id=? AND post_or_reply_id=?");
        HashMap<String,Integer> hash=getLikedPosts(user_id,deviceID);
        globalGetPost.clearParameters();
        globalGetPost.setBoolean(1,true);
        ResultSet searchResults=globalGetPost.executeQuery();
        JSONArray jsonArray=new JSONArray();
        while (searchResults.next()) {
            if (!searchResults.getBoolean("is_deleted")) {
                blockedPosts.clearParameters();
                blockedPosts.setString(1, user_id);
                blockedPosts.setString(2, deviceID);
                blockedPosts.setString(3, searchResults.getNString("post_id"));
                ResultSet blocks = blockedPosts.executeQuery();
                if (!blocks.next())
                {
                    getPostsByUser.clearParameters();
                    getPostsByUser.setString(1, deviceID);
                    ResultSet postsUser = getPostsByUser.executeQuery();

                    String postID = searchResults.getNString("post_id");
                    String clear_post = searchResults.getNString("clear_text");
                    Date date = searchResults.getDate("post_date");
                    Time time = searchResults.getTime("post_date");
                    int likes = searchResults.getInt("likes");
                    String city = searchResults.getNString("city");
                    String country = searchResults.getNString("country");
                    double lat = searchResults.getDouble("lati_v");
                    double long_v = searchResults.getDouble("long_v");
                    int reply_count = searchResults.getInt("reply_count");
                    String device_id=searchResults.getString("device_id");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("post_id", postID);
                    jsonObject.put("post_date", date);
                    jsonObject.put("post_time", time);
                    jsonObject.put("text", clear_post);
                    jsonObject.put("likes", likes);
                    jsonObject.put("city", city);
                    jsonObject.put("country", country);
                    jsonObject.put("lat", lat);
                    jsonObject.put("log", long_v);
                    if (searchResults.getBoolean("is_image"))
                    {
                        String img=getMainImage(searchResults.getBlob("image"));
                        jsonObject.put("img",img);
                    }
                    if (reply_count < 108) {
                        jsonObject.put("can_reply", true);
                    } else {
                        jsonObject.put("can_reply", false);
                    }
                    while (postsUser.next()) {
                        if (postsUser.getNString("post_id").equals(postID)) {
                            jsonObject.put("self", true);
                            break;
                        }
                    }
                    getrepliesCountForPosts.clearParameters();
                    getrepliesCountForPosts.setString(1, postID);
                    getrepliesCountForPosts.setBoolean(2,false);
                    ResultSet repliesCount = getrepliesCountForPosts.executeQuery();
                    if (repliesCount.next()) {
                        jsonObject.put("reply_count", repliesCount.getInt("count(*)"));
                    }
                    if (hash.containsKey(postID)) {
                        jsonObject.put("like_res", hash.get(postID));
                    }

                    postsUser.close();
                    repliesCount.close();
                    blocks.close();

                    jsonArray.put(jsonObject);

                }

                blocks.close();


            }
        }
        searchResults.close();

        return jsonArray;
    }
    public JSONArray getUsersPosts(JSONObject mainJSON) throws SQLException, IOException {
        getProPostsByUser=connection.prepareStatement("SELECT * FROM posts where user_id=? AND device_id=? order by post_date DESC");
        String deviceID=mainJSON.getString("app_id");
        String user_id=mainJSON.getString("user_id");
        HashMap<String,Integer> hash=getLikedPosts(user_id,deviceID);
        getProPostsByUser.clearParameters();
        getProPostsByUser.setString(1,user_id);
        getProPostsByUser.setString(2,deviceID);
        ResultSet searchResults=getProPostsByUser.executeQuery();
        JSONArray jsonArray=new JSONArray();
        while (searchResults.next()) {
            if (!searchResults.getBoolean("is_deleted"))
            {
                String postID = searchResults.getNString("post_id");
                String clear_post = searchResults.getNString("clear_text");
                Date date = searchResults.getDate("post_date");
                Time time = searchResults.getTime("post_date");
                int likes = searchResults.getInt("likes");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("post_id", postID);
                jsonObject.put("post_date", date);
                jsonObject.put("post_time", time);
                jsonObject.put("text", clear_post);
                jsonObject.put("likes", likes);
                jsonObject.put("self", true);
                jsonObject.put("can_reply",true);
                if (searchResults.getBoolean("is_image"))
                {
                    String img=getMainImage(searchResults.getBlob("image"));
                    jsonObject.put("img",img);
                }
                getrepliesCountForPosts.clearParameters();
                getrepliesCountForPosts.setString(1, postID);
                getrepliesCountForPosts.setBoolean(2,false);

                ResultSet repliesCount = getrepliesCountForPosts.executeQuery();
                if (repliesCount.next()) {
                    jsonObject.put("reply_count", repliesCount.getInt("count(*)"));
                }
                if (hash.containsKey(postID)) {
                    jsonObject.put("like_res", hash.get(postID));
                }
                repliesCount.close();
                jsonArray.put(jsonObject);
            }
        }
        searchResults.close();
        return jsonArray;
    }
    public JSONArray userReplies(JSONObject mainJSON) throws SQLException, IOException {
        getProPostsByUser=connection.prepareStatement("SELECT * FROM posts where user_id=? AND device_id=? order by post_date DESC");
        String device_id=mainJSON.getString("app_id");
        String _id= (String) mainJSON.get("user_id");
        HashMap<String,Integer> replies=getAllReplies(device_id);
        HashMap<String,Integer> hash=getLikedPosts(_id,device_id);
        ResultSet searchResults=getAllPosts.executeQuery();
        PreparedStatement blockedPosts=connection.prepareStatement("SELECT * FROM report_posts WHERE user_id=? AND device_id=? AND post_or_reply_id=?");
        JSONArray jsonArray=new JSONArray();
        while (searchResults.next()) {
            blockedPosts.clearParameters();
            blockedPosts.setString(1,_id);
            blockedPosts.setString(2,device_id);
            blockedPosts.setString(3,searchResults.getNString("post_id"));
            ResultSet blocked=blockedPosts.executeQuery();

            if (!searchResults.getBoolean("is_deleted")) {
                if (!blocked.next())
                {
                    if (replies.containsKey(searchResults.getNString("post_id")))
                    {
                        getProPostsByUser.clearParameters();
                        getProPostsByUser.setString(1, _id);
                        getProPostsByUser.setString(2, device_id);
                        ResultSet postsUser = getProPostsByUser.executeQuery();
                        String postID = searchResults.getNString("post_id");
                        String d_id=searchResults.getString("device_id");
                        String clear_post = searchResults.getNString("clear_text");
                        Date date = searchResults.getDate("post_date");
                        Time time = searchResults.getTime("post_date");
                        int likes = searchResults.getInt("likes");
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("post_id", postID);
                        jsonObject.put("post_date", date);
                        jsonObject.put("post_time", time);
                        jsonObject.put("text", clear_post);
                        jsonObject.put("likes", likes);
                        jsonObject.put("can_reply",true);
                        if (searchResults.getBoolean("is_image"))
                        {
                            String img=getMainImage(searchResults.getBlob("image"));
                            jsonObject.put("img",img);
                        }
                        while (postsUser.next()) {
                            if (postsUser.getNString("post_id").equals(postID)) {
                                jsonObject.put("self", true);
                                break;
                            }
                        }
                        getrepliesCountForPosts.clearParameters();
                        getrepliesCountForPosts.setString(1, postID);
                        getrepliesCountForPosts.setBoolean(2,false);

                        ResultSet repliesCount = getrepliesCountForPosts.executeQuery();
                        if (repliesCount.next()) {
                            jsonObject.put("reply_count", repliesCount.getInt("count(*)"));
                        }
                        if (hash.containsKey(postID)) {
                            jsonObject.put("like_res", hash.get(postID));
                        }
                        repliesCount.close();
                        postsUser.close();
                        jsonArray.put(jsonObject);
                    }
                }


            }
        }
        searchResults.close();
        return jsonArray;

    }
    public HashMap<String,Integer> getAllReplies(String device_id) throws SQLException {
        getUsersReplies.clearParameters();
        getUsersReplies.setString(1,device_id);
        ResultSet res=getUsersReplies.executeQuery();
        HashMap<String,Integer> hash=new HashMap<>();

        while (res.next())
        {
            hash.put(res.getNString("post_id"),res.getInt("likes"));
        }
        res.close();
        return hash;

    }
    public String getMainImage(Blob image) throws IOException, SQLException {
        byte[] bytes= image.getBytes(1, (int) image.length());
        BASE64Encoder encode=new BASE64Encoder();
        String img=encode.encode(bytes);
        image.free();
        return img;
    }

}
