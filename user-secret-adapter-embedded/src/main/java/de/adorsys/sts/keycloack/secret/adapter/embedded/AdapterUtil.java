package de.adorsys.sts.keycloack.secret.adapter.embedded;

import de.adorsys.sts.resourceserver.persistence.InMemoryResourceServerRepository;
import de.adorsys.sts.resourceserver.persistence.ResourceServerRepository;
import de.adorsys.sts.resourceserver.service.EncryptionService;
import de.adorsys.sts.resourceserver.service.KeyRetrieverService;
import de.adorsys.sts.resourceserver.service.ResourceServerManagementProperties;
import de.adorsys.sts.resourceserver.service.ResourceServerService;

public class AdapterUtil {
	public static final UserSecretAdapterEmbedded init(){
		ResourceServerRepository resourceServerRepository = new InMemoryResourceServerRepository();
		ResourceServerService resourceServerService = new ResourceServerService(resourceServerRepository);
		ResourceServerManagementProperties resourceServerManagementProperties = new EnvPropsResourceServerManagementProperties();
		KeyRetrieverService keyRetrieverService = new KeyRetrieverService(resourceServerService, resourceServerManagementProperties);
		EncryptionService encryptionService = new EncryptionService(keyRetrieverService);

		return new UserSecretAdapterEmbedded(resourceServerService, encryptionService);

	}
}
