/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.mapping;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.defaults.DefaultSqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An actual SQL String got form an {@link SqlSource} after having processed any dynamic content.
 * The SQL may have SQL placeholders "?" and an list (ordered) of an parameter mappings 
 * with the additional information for each parameter (at least the property name of the input object to read 
 * the value from). 
 * </br>
 * Can also have additional parameters that are created by the dynamic language (for loops, bind...).
 */
/**
 * @author Clinton Begin
 */
/**
 * 绑定的SQL,是从SqlSource而来，将动态内容都处理完成得到的SQL语句字符串，其中包括?,还有绑定的参数
 * 
 */
public class BoundSql {

  private String sql;
  private List<ParameterMapping> parameterMappings;
  private Object parameterObject;
  private Map<String, Object> additionalParameters;
  private MetaObject metaParameters;

  public BoundSql(Configuration configuration, String sql, List<ParameterMapping> parameterMappings, Object parameterObject) {
    this.sql = sql;
    this.parameterMappings = parameterMappings;
    this.parameterObject = parameterObject;
    this.additionalParameters = new HashMap<String, Object>();
    this.metaParameters = configuration.newMetaObject(additionalParameters);
  }

  public String getSql() {
    return sql;
  }

  public List<ParameterMapping> getParameterMappings() {
    return parameterMappings;
  }

  public Object getParameterObject() {
    return parameterObject;
  }

  public boolean hasAdditionalParameter(String name) {
    return metaParameters.hasGetter(name);
  }

  public void setAdditionalParameter(String name, Object value) {
    metaParameters.setValue(name, value);
  }

  public Object getAdditionalParameter(String name) {
    return metaParameters.getValue(name);
  }

  /**
   * 获取完整的sql
   *
   * @return
   */
  public String formatSql(Object parameter) {
    return formatSql(this.sql.replace("\n", ""), parameter);
  }

  /**
   * 获取完整的sql
   *
   * @return
   */
  public String formatSql(String sql, Object parameter) {
    if (parameter instanceof DefaultSqlSession.StrictMap) {
      String newSql = sql;

      DefaultSqlSession.StrictMap strictMap = (DefaultSqlSession.StrictMap) parameter;
      for (Object key : strictMap.keySet()) {
        if ("collection".equals(key) || "list".equals(key) || "array".equals(key)) {
          Object value = strictMap.get(key);
          if (value instanceof List) {
            for (Object val : (List) value) {
              newSql = this.formatFirstSql(newSql, val);
            }
          } else if (value.getClass().isArray()) {
            if (value.toString().contains("Integer")) {
              Integer[] newValue = (Integer[]) value;
              for (Integer v : newValue) {
                newSql = this.formatFirstSql(newSql, v);
              }

            } else {

            }
          }
        }
      }
      return newSql;

    }
    return this.formatFirstSql(sql, parameter);
  }

  /**
   * 提交第一个问号
   *
   * @param sql
   * @param parameter
   * @return
   */
  public String formatFirstSql(String sql, Object parameter) {
    if (!sql.contains("?")) {
      return sql;
    }
    if (parameter instanceof String) {
      return sql.replaceFirst("\\?", "'" + parameter.toString() + "'");

    }
    return sql.replaceFirst("\\?", parameter.toString());
  }

}
