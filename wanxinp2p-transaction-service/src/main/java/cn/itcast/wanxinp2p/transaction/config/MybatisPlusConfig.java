package cn.itcast.wanxinp2p.transaction.config;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * <P>
 * Mybatis-Plus 配置
 * </p>
 *
 * @author zhupeiyuan
 * @since 2019-05-10
 */
@Configuration
@MapperScan("cn.itcast.wanxinp2p.**.mapper")
public class MybatisPlusConfig {

//	/**
//	 * 分页插件，自动识别数据库类型
//	 */
//	@Bean
//	public PaginationInterceptor paginationInterceptor() {
//		return new PaginationInterceptor();
//	}
//
////	public PaginationInterceptor paginationInterceptor() {
////		PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
////		// 设置请求的页面大于最大页后操作，true调回到首页，false 继续请求  默认false
////		paginationInterceptor.setOverflow(false);
////		// 设置最大单页限制数量，默认 500 条，-1 不受限制
////		paginationInterceptor.setLimit(500);
////		return paginationInterceptor;
////	}
//
//	/**
//	 * 启用性能分析插件
//	 */
//	@Bean
//	public PerformanceInterceptor performanceInterceptor(){
//		return new PerformanceInterceptor();
//	}
}