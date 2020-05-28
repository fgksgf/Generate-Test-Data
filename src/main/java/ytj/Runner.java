package ytj;

import jhx.bean.Person;
import jhx.bean.QueryCondition;
import ytj.QueryResult;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.util.List;
import java.util.ArrayList;
import java.io.FileReader;
import java.lang.reflect.Field;


public abstract class Runner {

    private final String fileName;

    public Runner(String filename) {
        this.fileName = filename;
    }

    public final String getFileName() {
        return this.fileName;
    }

    public abstract void beforeCreateIndex(String field);

    public abstract void createIndex(String field);  // Timing for this method

    public abstract void afterCreateIndex(String field);

    public abstract void beforeDeleteIndex(String field);

    public abstract void deleteIndex(String field);  // Timing for this method

    public abstract void afterDeleteIndex(String field);

    public QueryResult queryWithoutIndex(QueryCondition condition) {
        Gson gson = new Gson();
        Field field = null;
        ArrayList<Long> result = new ArrayList<Long>();
        try {
            field = Person.class.getDeclaredField(condition.getField());
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            System.out.println("Failed: get Person Field.");
            return null;
        }

        try {
            JsonReader reader = new JsonReader(new FileReader(this.fileName));

            reader.beginArray();
            while(reader.hasNext()) {
                Person p = gson.fromJson(reader, Person.class);
                boolean flag = false;

                if (condition.isTypeOne()) {
                    int v = (int) field.get(p);
                    switch (condition.getOperator()) {
                        case "<":
                            if (v < condition.getValue()) flag = true;
                            break;
                        case "<=":
                            if (v <= condition.getValue()) flag = true;
                            break;
                        case ">":
                            if (v > condition.getValue()) flag = true;
                            break;
                        case ">=":
                            if (v >= condition.getValue()) flag = true;
                            break;
                        case "==":
                            if (v == condition.getValue()) flag = true;
                            break;
                    }
                } else {
                    int v = (int) field.get(p);
                    String leftOp = condition.getLeftOperator();
                    String rightOp = condition.getRightOperator();

                    if (leftOp.equals("<") && rightOp.equals("<")) {
                        if (condition.getLeftValue() < v && v < condition.getRightValue()) {
                            flag = true;
                        }
                    } else if (leftOp.equals("<") && rightOp.equals("<=")) {
                        if (condition.getLeftValue() < v && v <= condition.getRightValue()) {
                            flag = true;
                        }
                    } else if (leftOp.equals("<=") && rightOp.equals("<")) {
                        if (condition.getLeftValue() <= v && v < condition.getRightValue()) {
                            flag = true;
                        }
                    } else if (leftOp.equals("<=") && rightOp.equals("<=")) {
                        if (condition.getLeftValue() <= v && v <= condition.getRightValue()) {
                            flag = true;
                        }
                    }
                }
                
                if (flag) {
                    result.add(p.getId());
                }
            }
            reader.endArray();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed: generate result.");
            return null;
        }
        return new QueryResult(result); 
    }

    public abstract QueryResult queryWithIndex(QueryCondition condition);  // Timing for this method

}
