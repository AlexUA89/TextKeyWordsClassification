package com.welabeldata.archipelo.task1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class DbAdapter {


    public static final String DEV = "DEV";
    public static final String NON_DEV = "NON-DEV";
    public static final String DEFAULT_CLASSIFICATION = NON_DEV;

    Connection conn;

    public DbAdapter() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String url = "jdbc:postgresql://104.198.37.220:5432/postgres";
        Properties props = new Properties();
        props.setProperty("user", "labeler");
        props.setProperty("password", "labeling_archipelo");
        conn = DriverManager.getConnection(url, props);
        //        String url = "jdbc:postgresql://localhost/test?user=fred&password=secret&ssl=true";
        //        Connection conn = DriverManager.getConnection(url);
    }

    public UUID getUserIdByName(String userName) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select id from devs_strings.labelers where name = '" + userName + "'");
        if (rs.next()) {
            return UUID.fromString(rs.getString(1));
        }
        return null;
    }

    public Job getJobIdByName(String jobName) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select id, persons_per_task, name from devs_strings.job where name = '" + jobName + "'");
        if (rs.next()) {
            return new Job(UUID.fromString(rs.getString(1)), rs.getInt(2), rs.getString(3));
        }
        return null;
    }

    public List<TaskWithResult> getAllCurrentTasksForUser(UUID userId, UUID job_id) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select t.id, t.job_id, t.query, t.link, t.title, t.description, t.content, t.words, r.id, r.result_json, r.saved_date from devs_strings.results r " +
                " right join devs_strings.tasks t on t.id = r.task_id " +
                " where r.task_id in (select id from devs_strings.tasks where job_id = '" + job_id + "') and r.labeler_id = '" + userId + "' order by r.created_time;");
        List<TaskWithResult> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        while (rs.next()) {
            result.add(new TaskWithResult(UUID.fromString(rs.getString(1)), UUID.fromString(rs.getString(2)),
                    rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7),
                    mapStringToListOfKeyWords(rs.getString(8)),
                    UUID.fromString(rs.getString(9)),
                    mapStringToListOfClassKeyWords(rs.getString(10)),
                    Optional.ofNullable(rs.getTimestamp(11)).map(Timestamp::toLocalDateTime).orElse(null)));
        }
        return result;
    }

    public TaskWithResult getNextTaskForUser(UUID userId, UUID jobId, Integer usersPerTask) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select t.id, t.job_id, t.query, t.link, t.title, t.description, t.content, t.words from devs_strings.tasks t " +
                " where t.id not in(select task_id from devs_strings.results where labeler_id = '" + userId + "') " +
                " and t.job_id = '" + jobId + "' " +
                " and t.id not in (select task_id from (select count(*) as am, task_id from devs_strings.results group by task_id) temp where temp.am >= " + usersPerTask + ") limit 1;");

        if (rs.next()) {
            UUID resultId = UUID.randomUUID();
            stmt = conn.createStatement();
            stmt.executeUpdate("INSERT INTO devs_strings.results (id, task_id, labeler_id) VALUES ('" + resultId + "','" + rs.getString(1) + "', '" + userId + "')");
            return new TaskWithResult(UUID.fromString(rs.getString(1)), UUID.fromString(rs.getString(2)),
                    rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7),
                    mapStringToListOfKeyWords(rs.getString(8)), resultId,
                    new ArrayList<>(), null);
        }
        return null;
    }

    public TaskWithResult getNextTaskForExpert(UUID userId, UUID jobId, Integer usersPerTask) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select t.id, t.job_id, t.query, t.link, t.title, t.description, t.content, t.words from devs_strings.tasks t " +
                " where t.id not in(select task_id from devs_strings.results where labeler_id = '" + userId + "') " +
                " and t.job_id = '" + jobId + "' " +
                " and t.id in (select task_id from (select count(*) as am, task_id from devs_strings.results group by task_id) temp where temp.am = " + usersPerTask + ") limit 1;");
        if (rs.next()) {
            UUID resultId = UUID.randomUUID();
            stmt = conn.createStatement();
            stmt.executeUpdate("INSERT INTO devs_strings.results (id, task_id, labeler_id) VALUES ('" + resultId + "','" + rs.getString(1) + "', '" + userId + "')");
            return new TaskWithResult(UUID.fromString(rs.getString(1)), UUID.fromString(rs.getString(2)),
                    rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7),
                    mapStringToListOfKeyWords(rs.getString(8)), resultId,
                    new ArrayList<>(), null);
        }
        return null;
    }

    public void saveTaskWithResult(TaskWithResult taskToSave) throws SQLException {
        Statement stmt = conn.createStatement();
        ObjectMapper mapper = new ObjectMapper();
        List<ClassifiedKeyWord> result = taskToSave.getClassifiedKeyWords()
                .stream().sorted(Comparator.comparingInt(o -> o.getStartPos() + o.getEndPos()))
                .collect(Collectors.toList());
        try {
            stmt.executeUpdate("update devs_strings.results  set result_json = '" + mapper.writeValueAsString(result)
                    + "', saved_date = now() where id = '" + taskToSave.getResultId() + "' ");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        taskToSave.savedDate = LocalDateTime.now();
    }

    public static class ClassifiedKeyWord extends KeyWord {
        private String classification;

        public ClassifiedKeyWord(KeyWord keyWord, String classification) {
            super(keyWord.getStartPos(), keyWord.getEndPos(), keyWord.getType());
            this.classification = classification;
        }

        public ClassifiedKeyWord() {
        }

        public String getClassification() {
            return classification;
        }

        public void setClassification(String classification) {
            this.classification = classification;
        }
    }

    public static class TaskWithResult extends Task {
        private UUID resultId;
        private List<ClassifiedKeyWord> classifiedKeyWords;
        private LocalDateTime savedDate;

        public TaskWithResult(UUID id, UUID jobId, String query, String link, String title, String description,
                              String content, List<KeyWord> keyWords, UUID resultId,
                              List<ClassifiedKeyWord> classifiedKeyWords,
                              LocalDateTime savedDate) {
            super(id, jobId, query, link, title, description, content, keyWords);
            this.resultId = resultId;
            this.classifiedKeyWords = classifiedKeyWords;
            this.savedDate = savedDate;
        }

        public UUID getResultId() {
            return resultId;
        }

        public List<ClassifiedKeyWord> getClassifiedKeyWords() {
            return classifiedKeyWords;
        }

        public LocalDateTime getSavedDate() {
            return savedDate;
        }
    }

    public static class KeyWord {
        private int startPos;
        private int endPos;
        private String type;

        public KeyWord(int startPos, int endPos, String type) {
            this.startPos = startPos;
            this.endPos = endPos;
            this.type = type;
        }

        public KeyWord() {
        }

        public int getStartPos() {
            return startPos;
        }

        public void setStartPos(int startPos) {
            this.startPos = startPos;
        }

        public int getEndPos() {
            return endPos;
        }

        public void setEndPos(int endPos) {
            this.endPos = endPos;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class Task {
        private final UUID id;
        private final UUID jobId;
        private final String query;
        private final String link;
        private final String title;
        private final String description;
        private final String content;
        private final List<KeyWord> keyWords;

        public Task(UUID id, UUID jobId, String query, String link, String title, String description,
                    String content, List<KeyWord> keyWords) {
            this.id = id;
            this.jobId = jobId;
            this.query = query;
            this.link = link;
            this.title = title;
            this.description = description;
            this.content = content;
            this.keyWords = keyWords;
        }

        public UUID getId() {
            return id;
        }

        public UUID getJobId() {
            return jobId;
        }

        public String getQuery() {
            return query;
        }

        public String getLink() {
            return link;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getContent() {
            return content;
        }

        public List<KeyWord> getKeyWords() {
            return keyWords;
        }
    }

    public static class Job {
        private final UUID id;
        private final Integer personPerTask;
        private final String name;

        public Job(UUID id, Integer personPerTask, String name) {
            this.id = id;
            this.personPerTask = personPerTask;
            this.name = name;
        }

        public UUID getId() {
            return id;
        }

        public Integer getPersonPerTask() {
            return personPerTask;
        }

        public String getName() {
            return name;
        }
    }

    private List<KeyWord> mapStringToListOfKeyWords(String data) {
        ObjectMapper mapper = new ObjectMapper();
        return Optional.ofNullable(data).map(res -> {
            try {
                return new ArrayList<>(Arrays.asList(mapper.readValue(res, KeyWord[].class)))
                        .stream().sorted(Comparator.comparingInt(o -> o.getStartPos() + o.getEndPos()))
                        .collect(Collectors.toList());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return new ArrayList<KeyWord>();
        }).orElse(new ArrayList<>());
    }

    private List<ClassifiedKeyWord> mapStringToListOfClassKeyWords(String data) {
        ObjectMapper mapper = new ObjectMapper();
        return Optional.ofNullable(data).map(res -> {
            try {
                return new ArrayList<>(Arrays.asList(mapper.readValue(res, ClassifiedKeyWord[].class)))
                        .stream().sorted(Comparator.comparingInt(o -> o.getStartPos() + o.getEndPos()))
                        .collect(Collectors.toList());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return new ArrayList<ClassifiedKeyWord>();
        }).orElse(new ArrayList<>());
    }


}
