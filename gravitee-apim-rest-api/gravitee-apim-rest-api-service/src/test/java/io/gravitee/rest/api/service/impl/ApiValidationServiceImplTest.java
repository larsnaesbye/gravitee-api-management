/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.rest.api.service.impl;

import static io.gravitee.apim.core.member.model.SystemRole.PRIMARY_OWNER;
import static io.gravitee.common.component.Lifecycle.State.STARTED;
import static io.gravitee.definition.model.DefinitionContext.MODE_FULLY_MANAGED;
import static io.gravitee.definition.model.DefinitionContext.ORIGIN_KUBERNETES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import inmemory.ApiQueryServiceInMemory;
import inmemory.CategoryQueryServiceInMemory;
import inmemory.MembershipQueryServiceInMemory;
import inmemory.RoleQueryServiceInMemory;
import inmemory.UserDomainServiceInMemory;
import io.gravitee.apim.core.api.domain_service.ApiHostValidatorDomainService;
import io.gravitee.apim.core.api.domain_service.VerifyApiPathDomainService;
import io.gravitee.apim.core.api.model.Api;
import io.gravitee.apim.core.category.domain_service.ValidateCategoryIdsDomainService;
import io.gravitee.apim.core.category.model.Category;
import io.gravitee.apim.core.documentation.domain_service.ValidatePagesDomainService;
import io.gravitee.apim.core.installation.query_service.InstallationAccessQueryService;
import io.gravitee.apim.core.member.domain_service.ValidateCRDMembersDomainService;
import io.gravitee.apim.core.membership.model.Membership;
import io.gravitee.apim.core.membership.model.Role;
import io.gravitee.apim.core.user.model.BaseUserEntity;
import io.gravitee.apim.core.validation.Validator;
import io.gravitee.definition.model.DefinitionContext;
import io.gravitee.definition.model.DefinitionVersion;
import io.gravitee.definition.model.Proxy;
import io.gravitee.definition.model.VirtualHost;
import io.gravitee.rest.api.model.api.ApiCRDEntity;
import io.gravitee.rest.api.model.api.ApiValidationResult;
import io.gravitee.rest.api.service.common.ExecutionContext;
import io.gravitee.rest.api.service.common.GraviteeContext;
import io.gravitee.rest.api.service.common.UuidString;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Kamiel Ahmadpour (kamiel.ahmadpour at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class ApiValidationServiceImplTest {

    private final CategoryQueryServiceInMemory categoryQueryService = new CategoryQueryServiceInMemory();

    private final ApiQueryServiceInMemory apiQueryService = new ApiQueryServiceInMemory();

    private final InstallationAccessQueryService installationAccessQueryService = mock(InstallationAccessQueryService.class);

    private final ApiHostValidatorDomainService apiHostValidatorDomainService = mock(ApiHostValidatorDomainService.class);

    @Spy
    private VerifyApiPathDomainService pathValidator = new VerifyApiPathDomainService(
        apiQueryService,
        installationAccessQueryService,
        apiHostValidatorDomainService
    );

    @Spy
    private ValidateCategoryIdsDomainService validateCategoryIdsDomainService = new ValidateCategoryIdsDomainService(categoryQueryService);

    private final UserDomainServiceInMemory userDomainService = new UserDomainServiceInMemory();
    private final RoleQueryServiceInMemory roleQueryService = new RoleQueryServiceInMemory();
    private final MembershipQueryServiceInMemory membershipQueryService = new MembershipQueryServiceInMemory();

    @Mock
    private ValidatePagesDomainService pagesValidator;

    @Spy
    private ValidateCRDMembersDomainService validateCRDMembersDomainService = new ValidateCRDMembersDomainService(
        userDomainService,
        roleQueryService,
        membershipQueryService
    );

    @InjectMocks
    private ApiValidationServiceImpl cut;

    private static final String API_ID = "id-api";
    private static final String API_CROSS_ID = "id-cross-api";
    private static final String DEFAULT_ORGANIZATION_ID = "DEFAULT";
    private static final String API_NAME = "myAPI";
    private static final String CATEGORY_KEY = "category-key";
    private final ExecutionContext executionContext = GraviteeContext.getExecutionContext();

    @Before
    public void init() {
        userDomainService.reset();
        userDomainService.initWith(
            List.of(
                BaseUserEntity.builder().id("user-id").source("memory").sourceId("user-id").organizationId(DEFAULT_ORGANIZATION_ID).build(),
                BaseUserEntity
                    .builder()
                    .id("primary_owner_id")
                    .source("memory")
                    .sourceId("primary_owner_id")
                    .organizationId(DEFAULT_ORGANIZATION_ID)
                    .build()
            )
        );
        roleQueryService.reset();
        roleQueryService.initWith(
            List.of(
                Role
                    .builder()
                    .name(PRIMARY_OWNER.name())
                    .referenceType(Role.ReferenceType.ORGANIZATION)
                    .referenceId(DEFAULT_ORGANIZATION_ID)
                    .id("primary_owner_id")
                    .scope(Role.Scope.API)
                    .build(),
                Role
                    .builder()
                    .name("USER")
                    .referenceType(Role.ReferenceType.ORGANIZATION)
                    .referenceId(DEFAULT_ORGANIZATION_ID)
                    .id("user_role_id")
                    .scope(Role.Scope.API)
                    .build()
            )
        );
        categoryQueryService.reset();
        categoryQueryService.initWith(
            List.of(Category.builder().key(CATEGORY_KEY).name(CATEGORY_KEY).id(UuidString.generateRandom()).build())
        );
        apiQueryService.reset();
        when(pagesValidator.validateAndSanitize(any())).thenReturn(Validator.Result.ofBoth(null, null));
    }

    @Test
    public void shouldGivesWaningWithUnknownCategories() {
        ApiCRDEntity apiCRD = anApiCRDEntity();
        apiCRD.setCategories(Set.of("Unknown"));

        ApiValidationResult<ApiCRDEntity> validationResult = cut.validateAndSanitizeApiDefinitionCRD(executionContext, apiCRD);

        assertEquals(0, validationResult.getSevere().size());
        assertEquals(1, validationResult.getWarning().size());
        assertEquals("category [Unknown] is not defined in environment [DEFAULT]", validationResult.getWarning().get(0));
    }

    @Test
    public void shouldGivesNoWaningWithKnownCategories() {
        ApiCRDEntity apiCRD = anApiCRDEntity();
        apiCRD.setCategories(Set.of(CATEGORY_KEY));

        ApiValidationResult<ApiCRDEntity> validationResult = cut.validateAndSanitizeApiDefinitionCRD(executionContext, apiCRD);

        assertEquals(0, validationResult.getSevere().size());
        assertEquals(0, validationResult.getWarning().size());
    }

    @Test
    public void should_return_no_warning_with_existing_member() {
        ApiCRDEntity apiCRD = anApiCRDEntity();
        apiCRD.setMembers(List.of(new ApiCRDEntity.Member("memory", "user-id", "USER")));

        ApiValidationResult<ApiCRDEntity> validationResult = cut.validateAndSanitizeApiDefinitionCRD(executionContext, apiCRD);

        assertEquals(0, validationResult.getSevere().size());
        assertEquals(0, validationResult.getWarning().size());
    }

    @Test
    public void should_return_warning_with_unknown_member() {
        ApiCRDEntity apiCRD = anApiCRDEntity();
        apiCRD.setMembers(
            List.of(new ApiCRDEntity.Member("memory", "user-id", "USER"), new ApiCRDEntity.Member("memory", "unknown-user-id", "USER"))
        );

        ApiValidationResult<ApiCRDEntity> validationResult = cut.validateAndSanitizeApiDefinitionCRD(executionContext, apiCRD);

        assertEquals(0, validationResult.getSevere().size());
        assertEquals(1, validationResult.getWarning().size());
        assertEquals(
            "member [unknown-user-id] of source [memory] could not be found in organization [DEFAULT]",
            validationResult.getWarning().get(0)
        );
    }

    @Test
    public void should_return_warning_with_unknown_member_role() {
        ApiCRDEntity apiCRD = anApiCRDEntity();
        apiCRD.setMembers(List.of(new ApiCRDEntity.Member("memory", "user-id", "UNKNOWN")));

        ApiValidationResult<ApiCRDEntity> validationResult = cut.validateAndSanitizeApiDefinitionCRD(executionContext, apiCRD);

        assertEquals(0, validationResult.getSevere().size());
        assertEquals(1, validationResult.getWarning().size());
        assertEquals("member role [UNKNOWN] doesn't exist", validationResult.getWarning().get(0));
    }

    @Test
    public void should_return_error_with_primary_owner_role() {
        ApiCRDEntity apiCRD = anApiCRDEntity();
        apiCRD.setMembers(List.of(new ApiCRDEntity.Member("memory", "user-id", "PRIMARY_OWNER")));

        ApiValidationResult<ApiCRDEntity> validationResult = cut.validateAndSanitizeApiDefinitionCRD(executionContext, apiCRD);

        assertEquals(1, validationResult.getSevere().size());
        assertEquals(0, validationResult.getWarning().size());
        assertEquals("setting a member with the primary owner role is not allowed", validationResult.getSevere().get(0));
    }

    @Test
    public void should_return_error_changing_primary_owner_role() {
        membershipQueryService.initWith(
            List.of(
                Membership
                    .builder()
                    .memberType(Membership.Type.USER)
                    .referenceType(Membership.ReferenceType.API)
                    .referenceId(API_ID)
                    .roleId("primary_owner_id")
                    .memberId("primary_owner_id")
                    .build()
            )
        );

        ApiCRDEntity apiCRD = anApiCRDEntity();
        apiCRD.setMembers(List.of(new ApiCRDEntity.Member("memory", "primary_owner_id", "USER")));

        ApiValidationResult<ApiCRDEntity> validationResult = cut.validateAndSanitizeApiDefinitionCRD(executionContext, apiCRD);

        assertEquals(1, validationResult.getSevere().size());
        assertEquals(0, validationResult.getWarning().size());
        assertEquals("can not change the role of exiting primary owner [primary_owner_id]", validationResult.getSevere().get(0));
    }

    @Test
    public void should_return_error_with_conflicting_path() {
        apiQueryService.initWith(
            List.of(
                Api
                    .builder()
                    .id("conflicting-api-id")
                    .definitionVersion(DefinitionVersion.V2)
                    .apiDefinition(
                        io.gravitee.definition.model.Api
                            .builder()
                            .id("conflicting-api-id")
                            .proxy(Proxy.builder().virtualHosts(List.of(new VirtualHost("/echo"))).build())
                            .build()
                    )
                    .build()
            )
        );

        var crd = anApiCRDEntity();
        var validationResult = cut.validateAndSanitizeApiDefinitionCRD(executionContext, crd);

        assertEquals(1, validationResult.getSevere().size());
        assertEquals(0, validationResult.getWarning().size());
        assertEquals("Path [/echo/] already exists", validationResult.getSevere().get(0));
    }

    @Test
    public void should_return_error_validating_wrong_cron_expression() {
        when(pagesValidator.validateAndSanitize(any()))
            .thenReturn(Validator.Result.ofBoth(null, List.of(Validator.Error.severe("cron expression is invalid"))));

        ApiCRDEntity apiCRD = anApiCRDEntity();
        ApiValidationResult<ApiCRDEntity> validationResult = cut.validateAndSanitizeApiDefinitionCRD(executionContext, apiCRD);

        verify(pagesValidator, times(1)).validateAndSanitize(any());
        assertEquals(1, validationResult.getSevere().size());
        assertEquals(0, validationResult.getWarning().size());
        assertEquals("cron expression is invalid", validationResult.getSevere().get(0));
    }

    public ApiCRDEntity anApiCRDEntity() {
        ApiCRDEntity crd = new ApiCRDEntity();
        crd.setId(API_ID);
        crd.setCrossId(API_CROSS_ID);
        crd.setName(API_NAME);
        crd.setDescription(API_NAME);
        crd.setDefinitionContext(new DefinitionContext(ORIGIN_KUBERNETES, MODE_FULLY_MANAGED, ORIGIN_KUBERNETES));
        crd.setState(STARTED);
        crd.setProxy(Proxy.builder().virtualHosts(List.of(new VirtualHost("/echo"))).build());

        return crd;
    }
}
