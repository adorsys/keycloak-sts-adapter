package de.adorsys.sts.keycloack.secret.adapter.embedded;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEHeader.Builder;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;

import de.adorsys.sts.keycloack.secret.adapter.common.SecretAndAudModel;
import de.adorsys.sts.keycloack.secret.adapter.common.UserSecretAdapter;
import de.adorsys.sts.resourceserver.model.ResourceServer;
import de.adorsys.sts.resourceserver.service.EncryptionService;
import de.adorsys.sts.resourceserver.service.ResourceServerService;

public class UserSecretAdapterEmbedded implements UserSecretAdapter {
	
	private static String userMainSecretAttrName = UserSecretAdapter.USER_MAIN_SECRET_NOTE_KEY;

	ResourceServerService resourceServerService;
	EncryptionService encryptionService;

	public UserSecretAdapterEmbedded(ResourceServerService resourceServerService, EncryptionService encryptionService) {
		super();
		this.resourceServerService = resourceServerService;
		this.encryptionService = encryptionService;
	}

	/**
	 * The main secret is stored, encrypted with the user password. Remember that with this administrator
	 * based password reset will not work.
	 */
	@Override
	public String retrieveMainSecret(RealmModel realmModel, UserModel userModel, UserCredentialModel credentialModel) {
		List<String> userSecretClaimNameAttrs = userModel.getAttribute(userMainSecretAttrName);
		if(userSecretClaimNameAttrs==null || userSecretClaimNameAttrs.isEmpty()){
			return generateUserMainSecret(userModel, userMainSecretAttrName, credentialModel.getValue().getBytes());
		} else {
			return decrypt(userSecretClaimNameAttrs.iterator().next(), credentialModel.getValue().getBytes());
		}
	}

	@Override
	public Map<String, String> retrieveResourceSecrets(SecretAndAudModel secretAndAudModel, RealmModel realmModel,
			UserModel userModel) {
		List<String> audiances = secretAndAudModel.getAudiances();
		try {
			return readUserSecret(userModel, secretAndAudModel.getUserSecret(), audiances);
		} catch (UnsupportedEncodingException | JOSEException | ParseException e) {
			throw new IllegalStateException(e);
		}
	}
    private String generateUserMainSecret(UserModel userModel, String secretAttrName, byte[] secretEncryptionPassword) {
			String userMainSecretPlain = RandomStringUtils.randomGraph(16);
			Builder headerBuilder = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
			JWEObject jweObj = new JWEObject(headerBuilder.build(), new Payload(userMainSecretPlain));
	        try {
	            jweObj.encrypt(new DirectEncrypter(secretEncryptionPassword));
	        } catch (JOSEException e) {
	            throw new IllegalStateException(e);
	        }
	        String customSecretAttr = jweObj.serialize();
	        userModel.setAttribute(secretAttrName, Arrays.asList(customSecretAttr));
	        return userMainSecretPlain;
    }

    private Map<String, String> readUserSecret(UserModel userModel, String userMainSecret, List<String> audiences) throws JOSEException, UnsupportedEncodingException, ParseException {
    		Map<String, String> resourceSecrets = new HashMap<>();
    		for (String audience : audiences) {
	    		ResourceServer resourceServer = resourceServerService.getForAudience(audience);
	    		if(resourceServer==null) continue;
	    		
	    		String userSecretClaimName = resourceServer.getUserSecretClaimName();
	    		if(resourceSecrets.containsKey(userSecretClaimName)) continue;
	    		
	        List<String> userSecretClaimNameAttribute = userModel.getAttribute(userSecretClaimName);
	        byte[] userMainSecretBytes = userMainSecret.getBytes("UTF-8");
	        String userResourceSecretPlain;
	        if (userSecretClaimNameAttribute == null || userSecretClaimNameAttribute.isEmpty()) {
	        		userResourceSecretPlain = RandomStringUtils.randomNumeric(16);
	        		String customSecretAttrEnc = encrypt(userResourceSecretPlain, userMainSecretBytes);
	        		userModel.setAttribute(userSecretClaimName, Arrays.asList(customSecretAttrEnc));
	        } else {
	        		userResourceSecretPlain = decrypt(userSecretClaimNameAttribute.iterator().next(), userMainSecretBytes);
	        }
	        String userResourceSecretEncrypted = encryptionService.encryptFor(audience, userResourceSecretPlain);
	        resourceSecrets.put(userSecretClaimName, userResourceSecretEncrypted);
    		}
		return resourceSecrets;
    }
    
    private String encrypt(String plain, byte[] key){
		Builder headerBuilder = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
		JWEObject jweObj = new JWEObject(headerBuilder.build(), new Payload(plain));
		try {
			jweObj.encrypt(new DirectEncrypter(key));
		} catch (JOSEException e) {
			throw new IllegalStateException(e);
		}
		return jweObj.serialize();
    }
    
    private String decrypt(String encrypted, byte[] key){
		try {
			JWEObject jweObject = JWEObject.parse(encrypted); 
			jweObject.decrypt(new DirectDecrypter(key));
			return jweObject.getPayload().toString();
		} catch (JOSEException | ParseException e) {
			throw new IllegalStateException(e);
		}
    }

	@Override
	public void close() {
		
	}

}
