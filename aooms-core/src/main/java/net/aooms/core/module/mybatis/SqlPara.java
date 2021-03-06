package net.aooms.core.module.mybatis;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.aooms.core.databoss.DataBoss;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Sql script params
 * Created by 风象南(yuboon) on 2018/8/10
 */
public class SqlPara {

    private Logger logger = LoggerFactory.getLogger(getClass());

    // 空实例 无查询条件时使用
    public static final SqlPara SINGLETON = new SqlPara();
    // 查询条件
    private StringBuilder conditions = new StringBuilder();
    // 表别名
    private String alias = "";
    // 查询条件参数占位
    private static final String conditionsKey = "_ANDS_";

    // 内置key
    private static List<String> InternalKey = Arrays.asList(
            MyBatisConst.CRUD_QUERY_COUNT_PLACEHOLDER,
            MyBatisConst.CRUD_QUERY_PAGING_PLACEHOLDER
    );

    private Map<String, Object> paramMaps = Maps.newHashMap();

    private boolean isStart;
    private boolean isPaging;

    private int page,limit;

    /*// 数据量统计标记
    public static class Count{

    }*/

    public static SqlPara empty(){
        SqlPara sqlPara = new SqlPara();
        return sqlPara;
    }

    public static SqlPara fromDataBoss(){
        SqlPara sqlPara = new SqlPara();
        sqlPara.addParams(DataBoss.self().getPara().getData());
        sqlPara.addParams(DataBoss.self().getPara().getPathVars());
        return sqlPara;
    }

    public static SqlPara empty(Map<String,Object> params){
        SqlPara sqlPara = new SqlPara();
        sqlPara.addParams(params);
        return sqlPara;
    }

    public SqlPara set(String key, Object value){
        paramMaps.put(key, value);
        return this;
    }

    /**
     * 指定别名
     * @return
     */
    public SqlPara tableAlias(String alias){
        this.alias = alias;
        if(StrUtil.isNotBlank(alias)){
            this.alias = alias + ".";
        }
        return this;
    }

    /**
     * 分页
     * @param page
     * @param limit
     * @return
     */
    public SqlPara paging(int page,int limit){
        if(page > 0 && limit > 0){
            isPaging = true;
            this.page = page;
            this.limit = limit;
        }
        return this;
    }

    /**
     * 追加条件
     * @return
     */
    private boolean appendCondition(String join, SqlExpression sqlExpression){
        if(StrUtil.isNotBlank(String.valueOf(sqlExpression.getValue()))){
            //conditions.append(express + "'" + WAF.escapeSql(value) + "'");
            conditions.append(join + sqlExpression.toSqlString(alias));
            return true;
        }
        return false;
    }

    /**
     * 字段与参数名一致
     * @return
     */
    public SqlPara and(String... props){
        for (String prop: props) {
            appendCondition(whereOrAnd(),new SqlExpression(prop, Roper.Eq, prop, getString(prop)));
        }
        return this;
    }

    /**
     * 字段列明与参数名不一致
     * cp column - property
     * @return
     */
    public SqlPara andCp(String column,String valueProp){
        appendCondition(whereOrAnd(),new SqlExpression(column, Roper.Eq, valueProp, getString(valueProp)));
        return this;
    }

    /**
     * 条件参数
     * @return
     */
    /*public SqlPara orGroup(String[] columns,Roper[] ropers,String[] valueProps){
        if(columns.length != ropers.length && ropers.length != valueProps.length){
            throw new IllegalArgumentException("columns、ropers、valueProps length not equal !");
        }

        List<String> list = Lists.newArrayList();
        int size = columns.length;
        for (int i = 0 ;i < size; i++){
            String value = this.getString(valueProps[i]);
            if(StrUtil.isNotBlank(value)){
                list.add(alias + columns[i] + ropers[i].getOper() + "#{"+ valueProps[i] +"}");
            }
        }

        if(list.size() > 0){
            int index = 0;
            conditions.append(whereOrAnd() + "(");
            for (String exprss: list) {
                if(index > 0) conditions.append(" or ");
                conditions.append(exprss);
                index++;
            }
            conditions.append(")");
        }
        return this;
    }*/

    /**
     * 条件参数
     * @return
     */
    public SqlPara orGroup(SqlExpression... expressions){
        List<String> list = Lists.newArrayList();
        int size = expressions.length;
        for(SqlExpression expression : expressions){
            String value = this.getString(expression.getValueKey());
            if(StrUtil.isNotBlank(value)){
                list.add(alias + expression.toSqlString(alias));
            }
        }

        if(list.size() > 0){
            int index = 0;
            conditions.append(whereOrAnd() + "(");
            for (String exprss: list) {
                if(index > 0) conditions.append(" or ");
                conditions.append(exprss);
                index++;
            }
            conditions.append(")");
        }
        return this;
    }

    /**
     * 条件参数
     * @return
     */
    public SqlPara andLikeStart(String... props){
        for (String prop: props) {
            appendCondition(whereOrAnd(),new SqlExpression(prop, Roper.LikeStart, prop, getString(prop)));
        }
        return this;
    }

    public SqlPara andLikeStartCp(String column,String valueProp){
        appendCondition(whereOrAnd(),new SqlExpression(column, Roper.LikeStart, valueProp, getString(valueProp)));
        return this;
    }

    /**
     * 条件参数
     * @return
     */
    public SqlPara andLike(String... props){
        for (String prop: props) {
            appendCondition(whereOrAnd(),new SqlExpression(prop, Roper.Like, prop, getString(prop)));
        }
        return this;
    }

    public SqlPara andLikeCp(String column,String valueProp){
        appendCondition(whereOrAnd(),new SqlExpression(column, Roper.Like, valueProp, getString(valueProp)));
        return this;
    }

    /**
     * 大于
     * @return
     */
    public SqlPara gt(String... props){
        for (String prop: props) {
            appendCondition(whereOrAnd(), new SqlExpression(prop ,Roper.Gt,prop,getString(prop)));
        }
        return this;
    }

    public SqlPara gtCp(String column, String valueProp){
        appendCondition(whereOrAnd(),new SqlExpression(column ,Roper.Gt,valueProp,getString(valueProp)));
        return this;
    }

    /**
     * 小于
     * @returne
     */
    public SqlPara lt(String... props){
        for (String prop: props) {
            appendCondition(whereOrAnd(), new SqlExpression(prop ,Roper.Lt,prop,getString(prop)));
        }
        return this;
    }

    public SqlPara ltCp(String column, String valueProp){
        appendCondition(whereOrAnd(), new SqlExpression(column ,Roper.Lt,valueProp,getString(valueProp)));
        return this;
    }

    /**
     * 大于等于
     * @return
     */
    public SqlPara gte(String... props){
        for (String prop: props) {
            appendCondition(whereOrAnd(), new SqlExpression(prop, Roper.Gte, prop,getString(prop)));
        }
        return this;
    }

    public SqlPara gteCp(String column, String valueProp){
        appendCondition(whereOrAnd(),new SqlExpression(column, Roper.Gte, valueProp,getString(valueProp)));
        return this;
    }

    /**
     * 小于等于
     * @return
     */
    public SqlPara lte(String... props){
        for (String prop: props) {
            appendCondition(whereOrAnd(),new SqlExpression(prop, Roper.Lte, prop,getString(prop)));
        }
        return this;
    }

    public SqlPara lteCp(String column, String valueProp){
        appendCondition(whereOrAnd(),new SqlExpression(column, Roper.Lte, valueProp,getString(valueProp)));
        return this;
    }

    /**
     * where or and
     * @return
     */
    private String whereOrAnd(){
        if(!isStart){
            paramMaps.put(conditionsKey,conditions);
            isStart = true;
            //return " where ";
        }
        return " and ";
    }

    /**
     * 分页 获取DataBoss参数
     * @return
     */
    public SqlPara paging(){
        return paging(DataBoss.self().getPara().getPage(),DataBoss.self().getPara().getLimit());
    }

    public boolean isPaging(){
        return isPaging;
    }

    public SqlPara addParams(Map<String,Object> params){
        paramMaps.putAll(params);
        return this;
    }

    /**
     * 移除所有内置属性
     */
    public void removeInternalKey(){
        InternalKey.forEach(key -> {
            paramMaps.remove(key);
        });
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Object get(String key){
        return paramMaps.get(key);
    }

    public Map<String,Object> getParams(){
        return paramMaps;
    }

    public String getString(String key){
        Object value = this.get(key);
        if(value == null)return "";
        return String.valueOf(value);
    }

    /**
     * 获取字符串like格式
     *
     * for example:
     *      % + value + %
     *
     * @param key
     * @return
     */
    /*public String getStringLike(String key){
        String value = getString(key);
        if(StringUtils.isEmpty(value)){
            return "";
        }

        if(!value.endsWith("%")){
            value = "%" + value + "%";
            paramMaps.put(key + "_like_value", value);
        }
        return value;
    };*/

    /**
     * 获取字符串like格式
     *
     * for example:
     *      value + %
     *
     * @param key
     * @return
     */
    /*public String getStringLikeAfter(String key){
        String value = getString(key);
        if(StringUtils.isEmpty(value)){
            return "";
        }
        if(!value.endsWith("%")){
            value = value + "%";
            paramMaps.put(key + "_like_value", value);
        }
        return value;
    };*/

    public Integer getInteger(String key){
        return Integer.parseInt(getString(key));
    }

    public Long getLong(String key){
        return Long.parseLong(getString(key));
    }

    public Float getFloat(String key){
        return Float.parseFloat(getString(key));
    }

    public Double getDouble(String key){
        return Double.parseDouble(getString(key));
    }

    public Date getDate(String key){
        return DateUtil.parseDate(getString(key));
    }

    public DateTime getDateTime(String key){
        return DateUtil.parseDateTime(getString(key));
    }

    public Boolean getBoolean(String key){
        return Boolean.parseBoolean(getString(key));
    }

}
