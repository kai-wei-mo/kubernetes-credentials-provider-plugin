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
import com.cloudbees.jenkins.plugins.kubernetes_credentials_provider.SecretToCredentialConverter;
import com.cloudbees.jenkins.plugins.kubernetes_credentials_provider.SecretUtils;
import io.fabric8.kubernetes.api.model.Secret;
import org.jenkinsci.plugins.variant.OptionalExtension;
import com.spotio.jenkins.plugins.credentials.SpotPersonalAccessToken;

@OptionalExtension(requirePlugins = {"spotinst"})
public class SpotPersonalAccessTokenConvertor extends SecretToCredentialConverter {

    @Override
    public boolean canConvert(String type) {
        return "spotPersonalAccessToken".equals(type);
    }

    @Override
    public SpotPersonalAccessToken convert(Secret secret) throws CredentialsConvertionException {
        SecretUtils.requireNonNull(secret.getData(), "spotPersonalAccessToken definition contains no data");

        String tokenBase64 = SecretUtils.getNonNullSecretData(secret, "personalAccessToken", "spotPersonalAccessToken credential is missing the personalAccessToken");
        String token = SecretUtils.requireNonNull(SecretUtils.base64DecodeToString(tokenBase64), "spotPersonalAccessToken credential has an invalid personalAccessToken (must be base64 encoded UTF-8)");

        String idBase64 = SecretUtils.getNonNullSecretData(secret, "credentialsId", "spotPersonalAccessToken credential is missing the credentialsId");
        String id = SecretUtils.requireNonNull(SecretUtils.base64DecodeToString(idBase64), "spotPersonalAccessToken credential has an invalid credentialsId (must be base64 encoded UTF-8)");

        return new SpotPersonalAccessToken(
            // Scope
            SecretUtils.getCredentialScope(secret),
            // ID
            id,
            // Token
            token,
            // Description
            SecretUtils.getCredentialDescription(secret)
        );
    }
}
