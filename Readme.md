# Certificate Monitoring and Alarming
Spring boot project to monitor certificates lifecycle.

Project to monitoring certificates used to authenticate clients and servers during the communications between services. It aims to document the certificates and also notify them to the responsibles for its replacement in a timely manner. 

It supports certificates came from multiple sources such as Bitbucket or Amazon Certificate Manager and formats as keystore which is widely used to store certificates. Once fetched certificates and extracted their information, the application is able to notify them through different platforms ensuring that outdated certificates will not cause services downtime.

## Getting Started

To be able to use this project to monitor certificates you need to supply the right environment variables in order to make it work properly. Some of them are optional depending on the sources and platform you want to use. For further information you can check the link above and you can find out a pipeline build the project. 

		Available Options	
		




 | Env. Variables   |      Mandatory      | Default  | Meaning |
|----------|:-------------:|------:|------:|
| CMA_CRAWLER_SOURCES |  Yes |  bitbucket |Platform which the app should fetch keystores.|
| CMA_CRAWLER_BITBUCKET_USER	| Yes | -- | Bitbucket account with permissions to the target repositories. |
| CMA_CRAWLER_BITBUCKET_TOKEN	| Yes |	-- |	Valid token to access Bitbucket account.|
| CMA_CRAWLER_BITBUCKET_CREDENTIALS_TYPE	| Yes | -- |	keepass	Tool used to open the file which contains the credentials to open the keystores. |
| CMA_CRAWLER_BITBUCKET_CREDENTIALS_FILE	| Yes |	--	| Filename with the credentials to open the keystores. Don't forget to mention the repository below which contains this file. |
| CMA_CRAWLER_BITBUCKET_CREDENTIALS_PASS	| Yes |	--	| Password to open the file aforementioned. |
| CMA_CRAWLER_BITBUCKET_REPOSITORIES	| Yes	| --	| List of repositories which the app should looking for the certificates/keystores. | 
| CMA_CRAWLER_BITBUCKET_IGNORE-CERTS	| No	| --	| List of file with certificates/keystores that must be ignored. |
| CMA_EXTRACTOR_STRATEGIES	| Yes	| jks, acm	| List of sources that application should use to fetch certificates. |
| CMA_EXTRACTOR_AWS_REGION	| If extractor = acm	| --	| AWS region. |
| CMA_EXTRACTOR_AWS_ACCESSKEY	| If extractor= acm	| --	| AWS Access key. |
| CMA_EXTRACTOR_AWS_SECRET	| If extractor = acm	| --	| AWS Secret. |
| CMA_NOTIFIER_STRATEGIES	| Yes	| basic, confluence, email, jira	| Notifier strategies that the application should use to notify the certificates found during the build.  |
| CMA_NOTIFIER_EMAIL_FROM	| If notifier = email	| --	| Email sender. |
| CMA_NOTIFIER_EMAIL_TO	| If notifier = email	| --	| List of email recipients. |
| CMA_NOTIFIER_EMAIL_TEMPLATE	| If notifier = email	| -- |	Email template to be included within email structure. |
| CMA_NOTIFIER_EMAIL_SMTP_PROVIDER	| If notifier = email	| --	| SMTP Server provider. |
| CMA_NOTIFIER_EMAIL_SMTP_HOST	| If notifier = email	| --	| SMTP Server host address. |
| CMA_NOTIFIER_EMAIL_SMTP_PORT	| If notifier = email	| --	| SMTP Port. |
| CMA_NOTIFIER_EMAIL_SMTP_USERNAME	| No	| --	| SMTP Server Username. |
| CMA_NOTIFIER_EMAIL_SMTP_PASSWORD	| No	| --	| SMTP Server Password. |
| CMA_NOTIFIER_JIRA_BASEURL	| If notifier = jira	| --	| Jira API base url. By default use |
| CMA_NOTIFIER_JIRA_LABEL	| If notifier = jira	| --	| Label to identify ticket created. |
| CMA_NOTIFIER_JIRA_TOKEN	| If notifier = jira	| --	| Token to access Jira API with the right permissions to access the project mentioned below. |
| CMA_NOTIFIER_JIRA_PROJECTID	| If notifier = jira	| --	| Project ID where the ticket must take place. |
| CMA_NOTIFIER_JIRA_ISSUETYPE	| No	| --	| Issue Type ID. By default use 10001. |
| CMA_NOTIFIER_CONFLUENCE_BASEURL	| If notifier = confluence | |  |
| CMA_NOTIFIER_CONFLUENCE_TOKEN	| If notifier = confluence	| --	| Token to access Confluence API. |
| CMA_NOTIFIER_CONFLUENCE_CONTENTID	| If notifier = confluence	| --	| Content ID where the report should be published. |
| CMA_NOTIFIER_CONFLUENCE_ATTACHMENTID	| If notifier = confluence	| --	| Attachment ID where the report should be published. |

### Steps to implement the project:

The following steps are just a guidance you could have a simpler approach to implement the project so feel free to choose the option that fits better to your needs. Consider check the repository above mentioned to look at a real scenario using a pipeline to build the project.

1. Create a new branch from from master.
2. Create a new properties file (i.e example-project.properties) under resources/config with the variables aforementioned. All secret information should not be exposed along with other properties.  
3. Update .Jenkinsfile in stage Run where should take place the credentials within environment scope. Consider create these credentials in Jenkins Credentials and reference them here.
4. Still in .Jenkinsfile now you should update your configuration filename in first step of stage Run. For instance, if you created the file example-project.properties on the step 2 then the step should looks like this: sh 'docker build --build-arg CONFIG_NAME={example-project} -t cert-app:lts .'
5. Once you set the environment variables properties and mentioned the properties file you can't forget to mention your branch created in step 1 within the Pipeline Configuration to load properly your configurations.
