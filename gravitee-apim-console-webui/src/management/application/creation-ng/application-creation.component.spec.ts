/*
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { HarnessLoader } from '@angular/cdk/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpTestingController } from '@angular/common/http/testing';
import { GioSaveBarHarness } from '@gravitee/ui-particles-angular';

import { ApplicationCreationComponent } from './application-creation.component';
import { ApplicationCreationFormHarness } from './components/application-creation-form.harness';

import { CONSTANTS_TESTING, GioTestingModule } from '../../../shared/testing';
import { fakeApplicationTypes } from '../../../entities/application-type/ApplicationType.fixture';

describe('ApplicationCreationComponent', () => {
  let component: ApplicationCreationComponent;
  let fixture: ComponentFixture<ApplicationCreationComponent>;
  let loader: HarnessLoader;
  let applicationCreationForm: ApplicationCreationFormHarness;
  let httpTestingController: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NoopAnimationsModule, ApplicationCreationComponent, GioTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationCreationComponent);
    component = fixture.componentInstance;
    loader = TestbedHarnessEnvironment.loader(fixture);
    httpTestingController = TestBed.inject(HttpTestingController);
    fixture.autoDetectChanges();
    applicationCreationForm = await loader.getHarness(ApplicationCreationFormHarness);

    expectGetEnabledApplicationTypes();
  });

  it('should create application type=simple', async () => {
    await applicationCreationForm.setGeneralInformation('name', 'description', 'domain');
    await applicationCreationForm.setApplicationType('SIMPLE');

    await applicationCreationForm.setSimpleApplicationType('appType', 'appClientId');

    const saveBar = await loader.getHarness(GioSaveBarHarness);
    await saveBar.clickSubmit();

    expect(component.applicationPayload).toEqual({
      name: 'name',
      description: 'description',
      domain: 'domain',
      type: 'SIMPLE',
      appType: 'appType',
      appClientId: 'appClientId',
      oauthGrantTypes: null,
      oauthRedirectUris: [],
    });
  });

  it('should create application type=web', async () => {
    await applicationCreationForm.setGeneralInformation('name', 'description', 'domain');
    await applicationCreationForm.setApplicationType('WEB');

    await applicationCreationForm.setOAuthApplicationType(['Refresh Token'], ['redirectUri']);

    const saveBar = await loader.getHarness(GioSaveBarHarness);
    await saveBar.clickSubmit();

    expect(component.applicationPayload).toEqual({
      name: 'name',
      description: 'description',
      domain: 'domain',
      type: 'WEB',
      appType: null,
      appClientId: null,
      oauthGrantTypes: ['authorization_code', 'refresh_token'],
      oauthRedirectUris: ['redirectUri'],
    });
  });

  it('should create application type=BACKEND_TO_BACKEND', async () => {
    await applicationCreationForm.setGeneralInformation('name', 'description', 'domain');
    await applicationCreationForm.setApplicationType('BACKEND_TO_BACKEND');

    expect(await applicationCreationForm.getOauthRedirectUrisInput()).toEqual(null);

    const saveBar = await loader.getHarness(GioSaveBarHarness);
    await saveBar.clickSubmit();

    expect(component.applicationPayload).toEqual({
      name: 'name',
      description: 'description',
      domain: 'domain',
      type: 'BACKEND_TO_BACKEND',
      appType: null,
      appClientId: null,
      oauthGrantTypes: ['client_credentials'],
      oauthRedirectUris: [],
    });
  });

  function expectGetEnabledApplicationTypes() {
    const req = httpTestingController.expectOne({
      method: 'GET',
      url: `${CONSTANTS_TESTING.env.baseURL}/configuration/applications/types`,
    });
    req.flush(fakeApplicationTypes());
  }
});