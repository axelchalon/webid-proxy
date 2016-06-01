/*
 *  WebID-TLS Proxy
 *  Copyright (C) 2016 Danube Tech GmbH
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.danubetech.webidproxy.users;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import com.danubetech.webidproxy.config.Config;
import com.danubetech.webidproxy.ssl.MySSLSocketFactory;

public class WebIDRegistration {

	private static final Log log = LogFactory.getLog(WebIDRegistration.class);

	static void registerWebIDAccount(User user) throws IOException {

		String webid = webid(user);
		String host = host(user);

		List<NameValuePair> accountParameterMap = new ArrayList<NameValuePair> ();
		accountParameterMap.add(new BasicNameValuePair("username", user.getUsername()));
		accountParameterMap.add(new BasicNameValuePair("password", user.getPassword()));
		accountParameterMap.add(new BasicNameValuePair("spkac", user.getSpkac()));
		accountParameterMap.add(new BasicNameValuePair("webid", webid));
		accountParameterMap.add(new BasicNameValuePair("host", host));
		if (user.getName() != null) accountParameterMap.add(new BasicNameValuePair("name", user.getName()));
		if (user.getEmail() != null) accountParameterMap.add(new BasicNameValuePair("email", user.getEmail()));

		submit(accountEndpoint(user), accountParameterMap);
	}

	static void registerWebIDCert(User user) throws IOException {

		List<NameValuePair> certParameterMap = new ArrayList<NameValuePair> ();
		certParameterMap.add(new BasicNameValuePair("username", user.getUsername()));
		certParameterMap.add(new BasicNameValuePair("spkac", user.getSpkac()));
		submit(certEndpoint(user), certParameterMap);
	}

	private static String webid(User user) {

		if (Config.vhosts()) {

			return "https://" + user.getUsername() + "." + Config.webidHost() + "/profile/card#me";
		} else {

			return "https://" + Config.webidHost() + "/" + user.getUsername() + "/profile/card#me";
		}
	}

	private static String host(User user) {

		if (Config.vhosts()) {

			return user.getUsername() + "." + Config.webidHost();
		} else {

			return Config.webidHost();
		}
	}

	private static String accountEndpoint(User user) {

		if (Config.vhosts()) {

			return "https://" + user.getUsername() + "." + Config.webidHost() + "/" + "api/accounts/new";
		} else {

			return "https://" + Config.webidHost() + "/" + "api/accounts/new";
		}
	}

	private static String certEndpoint(User user) {

		if (Config.vhosts()) {

			return "https://" + user.getUsername() + "." + Config.webidHost() + "/" + "api/accounts/cert";
		} else {

			return "https://" + Config.webidHost() + "/" + "api/accounts/cert";
		}
	}

	private static void submit(String target, List<? extends NameValuePair> nameValuePairs) throws IOException {

		HttpClient httpClient = MySSLSocketFactory.getNewHttpClient(null);
		HttpPost httpPost = new HttpPost(target);
		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		HttpResponse httpResponse = httpClient.execute(httpPost);

		log.info("SUBMIT " + target + " " + nameValuePairs + " -> " + httpResponse.getStatusLine());
	}
}
