package cn.itcast.wanxinp2p.uaa.domain;

import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountLoginDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.common.util.StringUtil;
import cn.itcast.wanxinp2p.uaa.agent.AccountApiAgent;
import cn.itcast.wanxinp2p.uaa.common.utils.ApplicationContextHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class IntegrationUserDetailsAuthenticationHandler {

	/**
	 * 认证处理
	 * @param domain 用户域 ，如b端用户、c端用户等
	 * @param authenticationType  认证类型，如密码认证，短信认证等
	 * @param token
	 * @return
	 */
	public UnifiedUserDetails authentication(String domain, String authenticationType,
			UsernamePasswordAuthenticationToken token) {
		//在这里进行登录处理
		//取数据
		String username = token.getName();
		if(StringUtil.isBlank(username)){
			throw new BadCredentialsException("账户为空");
		}
		//对password进行判断空，用工具类
		if(token.getCredentials() == null){
			throw new BadCredentialsException("密码为空");
		}
		String password = (String) token.getCredentials();


		//远程调用统一账号服务，进行账号密码校验，用openFeign
		AccountLoginDTO accountLoginDTO = new AccountLoginDTO();
		accountLoginDTO.setMobile(username);
		accountLoginDTO.setPassword(password);
		accountLoginDTO.setDomain(domain);
		accountLoginDTO.setUsername(username);
		//通过类名.class得到代理对象
		AccountApiAgent accountApiAgent = (AccountApiAgent)ApplicationContextHelper.getBean(AccountApiAgent.class);
		//调用远程方法
		RestResponse<AccountDTO> response = accountApiAgent.login(accountLoginDTO);
		//判断是否成功
		if(response.getCode() != 0){
			throw new BadCredentialsException(response.getMsg());
		}

		//如果校验通过，用户对象封装到UnifiedUserDetails
		UnifiedUserDetails userDetails = new UnifiedUserDetails(response.getResult().getUsername(), password, AuthorityUtils.createAuthorityList());
		//把手机号也放进去
		userDetails.setMobile(response.getResult().getMobile());



		return userDetails;
		
	}

	private UnifiedUserDetails getUserDetails(String username) {
		Map<String, UnifiedUserDetails> userDetailsMap = new HashMap<>();
		userDetailsMap.put("admin",
				new UnifiedUserDetails("admin", "111111", AuthorityUtils.createAuthorityList("ROLE_PAGE_A", "PAGE_B")));
		userDetailsMap.put("xufan",
				new UnifiedUserDetails("xufan", "111111", AuthorityUtils.createAuthorityList("ROLE_PAGE_A", "PAGE_B")));

		userDetailsMap.get("admin").setDepartmentId("1");
		userDetailsMap.get("admin").setMobile("18611106983");
		userDetailsMap.get("admin").setTenantId("1");
		Map<String, List<String>> au1 = new HashMap<>();
		au1.put("ROLE1", new ArrayList<>());
		au1.get("ROLE1").add("p1");
		au1.get("ROLE1").add("p2");
		userDetailsMap.get("admin").setUserAuthorities(au1);
		Map<String, Object> payload1 = new HashMap<>();
		payload1.put("res", "res1111111");
		userDetailsMap.get("admin").setPayload(payload1);


		userDetailsMap.get("xufan").setDepartmentId("2");
		userDetailsMap.get("xufan").setMobile("18611106984");
		userDetailsMap.get("xufan").setTenantId("1");
		Map<String, List<String>> au2 = new HashMap<>();
		au2.put("ROLE2", new ArrayList<>());
		au2.get("ROLE2").add("p3");
		au2.get("ROLE2").add("p4");
		userDetailsMap.get("xufan").setUserAuthorities(au2);

		Map<String, Object> payload2 = new HashMap<>();
		payload2.put("res", "res222222");
		userDetailsMap.get("xufan").setPayload(payload2);

		return userDetailsMap.get(username);

	}

}
