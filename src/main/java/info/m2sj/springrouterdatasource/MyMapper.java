package info.m2sj.springrouterdatasource;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MyMapper {
    @Select("select db_name from route_test")
    String findDbName();
}
