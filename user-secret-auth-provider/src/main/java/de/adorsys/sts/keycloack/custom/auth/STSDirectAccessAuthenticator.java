package de.adorsys.sts.keycloack.custom.auth;

import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.directgrant.ValidatePassword;
import org.keycloak.credential.CredentialInput;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserCredentialModel;

import de.adorsys.sts.keycloack.secret.adapter.common.UserSecretAdapter;

/**
 * Retrieves scope of auth request and stores it in the
 * {@link UserCredentialModel} as note for use by the credential validation
 * layer to find out requested scopes.
 * 
 * On successful authentication, retrieves the user secret note and stores it in
 * the user session for use by the protocol mapper.
 * 
 * @author fpo
 *
 */
public class STSDirectAccessAuthenticator extends ValidatePassword {
	private static final String PROVIDER_ID = "sts-direct-access-authenticator";
	
	private UserSecretAdapter userSecretAdapter;
	
	@Override
	public void postInit(KeycloakSessionFactory factory) {
		super.postInit(factory);
		userSecretAdapter = factory.getProviderFactory(UserSecretAdapter.class).create(null);
	}

	@Override
	public String getDisplayType() {
		return "Custom Validate Password";
	}

	@Override
	public String getId() {
		return STSDirectAccessAuthenticator.PROVIDER_ID;
	}

	public Authenticator create(KeycloakSession session) {
		return this;
	}
	
	@Override
	public void authenticate(AuthenticationFlowContext context) {
		UserCredentialModel credentialModel = passwordAndScope(context);

		boolean valid = context.getSession().userCredentialManager().isValid(context.getRealm(), context.getUser(),
				new CredentialInput[] { credentialModel });

		if (!valid) {
			context.getEvent().user(context.getUser());
			context.getEvent().error("invalid_user_credentials");
			Response challengeResponse = this.errorResponse(Status.UNAUTHORIZED.getStatusCode(), "invalid_grant",
					"Invalid user credentials");
			context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
		} else {
			AuthenticatorUtil.addMainSecretToUserSession(userSecretAdapter, context, credentialModel);
			context.success();
		}
	}
	
	private UserCredentialModel passwordAndScope(AuthenticationFlowContext context){
		String password = this.retrievePassword(context);
		UserCredentialModel credentialModel = UserCredentialModel.password(password);

		Optional<String> scope = AuthenticatorUtil.readScope(context);
		scope.ifPresent(s -> credentialModel.setNote(Constants.CUSTOM_SCOPE_NOTE_KEY, s));
		return credentialModel;
	}
}
