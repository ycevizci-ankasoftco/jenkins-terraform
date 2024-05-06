pipeline {
    agent any
    
    parameters {
        string(name: 'location', defaultValue: '', description: 'Location')
    }
    
    environment {
        ARM_CLIENT_ID = credentials('ARM_CLIENT_ID')
        ARM_CLIENT_SECRET = credentials('ARM_CLIENT_SECRET')
        ARM_SUBSCRIPTION_ID = credentials('ARM_SUBSCRIPTION_ID')
        ARM_TENANT_ID = credentials('ARM_TENANT_ID')
    }

    stages {
        stage('Git Pull') {
            steps {
                script {
                    // Git kimlik bilgilerini ayarla
                    sh "git config user.email 'ycevizci@ankasoft.co'"
                    sh "git config user.name 'ycevizci-ankasoftco'"
                    
                    // GitHub'dan repo'yu çek
                    git branch: 'main', url: 'https://github.com/ycevizci-ankasoftco/jenkins-az.git'
                    sh "pwd"
                    sh "ls"
                }
            }
        }
        
        stage('Configure') {
            steps {
                script {
                    // Terraform.tfvars dosyasını güncelle
                    def configFile = 'terraform.tfvars'
            
                    // Dosyayı oku ve içeriği al
                    def configFileContent = readFile(configFile)
            
                    // location değişkenini güncelle
                    configFileContent = configFileContent.readLines().collect { line ->
                        if (line.startsWith("location")) {
                            return "location = ${params.location}\n" // Yeni satır ekle
                        } else {
                            return line
                        }
                    }.join('\n')
            
                    // Dosyayı güncellenmiş içerikle yaz
                    writeFile file: configFile, text: configFileContent
                    sh "cat terraform.tfvars"
        }
    }
}

        
        stage('Git Push') {
            steps {
                script {
                    // Değişiklikleri commit ve push et
                    sh "git add terraform.tfvars"
                    sh "git commit -m 'Updated terraform.tfvars via Jenkins pipeline'"
                    sh "git push -f https://ycevizci-ankasoftco:<token>@github.com/ycevizci-ankasoftco/terraform-module.git HEAD:main"
                }
            }
        }
        
        stage('Git Pull 2') {
            steps {
                script {
                    // Terraform Kodunu Çek
                    git branch: 'main', url: 'https://github.com/ycevizci-ankasoftco/terraform-module.git'
                }
            }
        }
        
        
        
        stage('Terraform Apply') {
            steps {
                script {
                    // Terraform Apply
                    sh "terraform init"
                    sh "terraform fmt"
                    sh "terraform apply --auto-approve"
                }
            }
        }
    }
}
