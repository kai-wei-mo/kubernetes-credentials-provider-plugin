/*
 * The MIT License
 *
 * Copyright 2024 @kai-wei-mo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.spotio.jenkins.plugins.credentials_provider.convertors;

import com.cloudbees.jenkins.plugins.kubernetes_credentials_provider.CredentialsConvertionException;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.spotio.jenkins.plugins.credentials.SpotPersonalAccessToken;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests SpotPersonalAccessTokenConvertor
 */
@RunWith(MockitoJUnitRunner.class)
public class SpotPersonalAccessTokenConvertorTest {

    @Test
    public void canConvert() throws Exception {
        SpotPersonalAccessTokenConvertor convertor = new SpotPersonalAccessTokenConvertor();
        assertThat("correct registration of valid type", convertor.canConvert("spotPersonalAccessToken"), is(true));
        assertThat("incorrect type is rejected", convertor.canConvert("something"), is(false));
    }

    @Test
    public void canConvertAValidSecret() throws Exception {
        SpotPersonalAccessTokenConvertor convertor = new SpotPersonalAccessTokenConvertor();

        try (InputStream is = get("valid.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            assertThat("The Secret was loaded correctly from disk", notNullValue());
            SpotPersonalAccessToken credential = convertor.convert(secret);
            assertThat(credential, notNullValue());
            assertThat("credential id is mapped correctly", credential.getId(), is("test-spot-token"));
            assertThat("credential description is mapped correctly", credential.getDescription(), is("Spot personal access token from Kubernetes"));
            assertThat("credential scope is mapped correctly", credential.getScope(), is(CredentialsScope.GLOBAL));
            assertThat("credential token is mapped correctly", credential.getToken().getPlainText(), is("test-token"));
        }
    }

    @Test
    public void failsToConvertWhenTokenMissing() throws Exception {
        SpotPersonalAccessTokenConvertor convertor = new SpotPersonalAccessTokenConvertor();

        try (InputStream is = get("missingToken.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            convertor.convert(secret);
            fail("Exception should have been thrown");
        } catch (CredentialsConvertionException cex) {
            assertThat(cex.getMessage(), containsString("missing the personalAccessToken"));
        }
    }

    @Test
    public void failsToConvertWhenIdMissing() throws Exception {
        SpotPersonalAccessTokenConvertor convertor = new SpotPersonalAccessTokenConvertor();

        try (InputStream is = get("missingId.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            convertor.convert(secret);
            fail("Exception should have been thrown");
        } catch (CredentialsConvertionException cex) {
            assertThat(cex.getMessage(), containsString("missing the credentialsId"));
        }
    }

    @Test
    public void failsToConvertWhenTokenCorrupt() throws Exception {
        SpotPersonalAccessTokenConvertor convertor = new SpotPersonalAccessTokenConvertor();

        try (InputStream is = get("corruptToken.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            convertor.convert(secret);
            fail("Exception should have been thrown");
        } catch (CredentialsConvertionException cex) {
            assertThat(cex.getMessage(), containsString("invalid personalAccessToken"));
        }
    }

    @Test
    public void failsToConvertWhenIdCorrupt() throws Exception {
        SpotPersonalAccessTokenConvertor convertor = new SpotPersonalAccessTokenConvertor();

        try (InputStream is = get("corruptId.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            convertor.convert(secret);
            fail("Exception should have been thrown");
        } catch (CredentialsConvertionException cex) {
            assertThat(cex.getMessage(), containsString("invalid credentialsId"));
        }
    }

    @Test
    public void failsToConvertWhenDataEmpty() throws Exception {
        SpotPersonalAccessTokenConvertor convertor = new SpotPersonalAccessTokenConvertor();

        try (InputStream is = get("void.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            convertor.convert(secret);
            fail("Exception should have been thrown");
        } catch (CredentialsConvertionException cex) {
            assertThat(cex.getMessage(), containsString("contains no data"));
        }
    }

    private static final InputStream get(String resource) {
        InputStream is = SpotPersonalAccessTokenConvertorTest.class.getResourceAsStream("SpotPersonalAccessTokenConvertorTest/" + resource);
        if (is == null) {
            fail("failed to load resource " + resource);
        }
        return is;
    }
}
