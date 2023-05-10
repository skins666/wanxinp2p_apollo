package cn.itcast.wanxinp2p.consumer.common;

import cn.itcast.wanxinp2p.api.account.model.LoginUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class SecurityUtil {

	/**
	 * 获取登录用户信息
	 *
	 * @return 登录用户信息
	 */
	public static LoginUser getUser() {
		// 从request上下文获取ServletRequestAttributes对象
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

		// 如果ServletRequestAttributes对象不为空，则从中获取HttpServletRequest对象
		if (servletRequestAttributes != null) {
			HttpServletRequest request = servletRequestAttributes.getRequest();

			// 从HttpServletRequest获取jsonToken属性值，并判断其类型是否为LoginUser
			Object jwt = request.getAttribute("jsonToken");
			if (jwt instanceof LoginUser) {
				// 如果是LoginUser类型，则直接返回
				return (LoginUser) jwt;
			}
		}

		// 如果ServletRequestAttributes对象为空或jsonToken属性值不是LoginUser类型，则返回一个新的LoginUser对象
		return new LoginUser();
	}


}
