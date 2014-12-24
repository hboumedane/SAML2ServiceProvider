/* 
 * Copyright 2014 Tirasa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.tirasa.saml.util;

import java.io.StringWriter;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.ws.security.SecurityPolicy;
import org.opensaml.ws.security.SecurityPolicyResolver;
import org.opensaml.ws.security.SecurityPolicyRule;
import org.opensaml.ws.security.provider.BasicSecurityPolicy;
import org.opensaml.ws.security.provider.HTTPRule;
import org.opensaml.ws.security.provider.MandatoryIssuerRule;
import org.opensaml.ws.security.provider.StaticSecurityPolicyResolver;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.util.XMLHelper;

public class SAMLUtils {

    public static SAMLMessageContext decodeSamlMessage(HttpServletRequest request, HttpServletResponse response) throws
            Exception {

        SAMLMessageContext<SAMLObject, SAMLObject, NameID> samlMessageContext
                = new BasicSAMLMessageContext<SAMLObject, SAMLObject, NameID>();

        HttpServletRequestAdapter httpServletRequestAdapter = new HttpServletRequestAdapter(request);
        samlMessageContext.setInboundMessageTransport(httpServletRequestAdapter);
        samlMessageContext.setInboundSAMLProtocol(SAMLConstants.SAML20P_NS);
        
        HttpServletResponseAdapter httpServletResponseAdapter = 
                new HttpServletResponseAdapter(response, request.isSecure());
        samlMessageContext.setOutboundMessageTransport(httpServletResponseAdapter);
        samlMessageContext.setPeerEntityRole(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

        SecurityPolicyResolver securityPolicyResolver = getSecurityPolicyResolver(request.isSecure());

        samlMessageContext.setSecurityPolicyResolver(securityPolicyResolver);
        HTTPPostDecoder samlMessageDecoder = new HTTPPostDecoder();
        
        samlMessageDecoder.decode(samlMessageContext);
        
        return samlMessageContext;
    }

    private static SecurityPolicyResolver getSecurityPolicyResolver(boolean isSecured) {
        SecurityPolicy securityPolicy = new BasicSecurityPolicy();
        HTTPRule httpRule = new HTTPRule(null, null, isSecured);
        MandatoryIssuerRule mandatoryIssuerRule = new MandatoryIssuerRule();
        List<SecurityPolicyRule> securityPolicyRules = securityPolicy.getPolicyRules();
        securityPolicyRules.add(httpRule);
        securityPolicyRules.add(mandatoryIssuerRule);
        return new StaticSecurityPolicyResolver(securityPolicy);
    }

    public static String SAMLObjectToString(final org.opensaml.xml.XMLObject samlObject) {
        try {
            org.opensaml.xml.io.Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(samlObject);
            org.w3c.dom.Element authDOM = marshaller.marshall(samlObject);
            StringWriter rspWrt = new StringWriter();
            XMLHelper.writeNode(authDOM, rspWrt);
            return rspWrt.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
