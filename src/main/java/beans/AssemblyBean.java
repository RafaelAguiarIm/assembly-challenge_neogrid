package beans;

import business.ConversationListFromFile;
import controller.AssemblyController;
import dao.AssemblyDao;
import dao.LinhasDeMontagensDao;
import infra.JPAUtil;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import models.Assembly;
import models.LinhasDeMontagens;
import org.primefaces.model.file.UploadedFile;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Named
@ManagedBean
@RequestScoped
public class AssemblyBean {

    public AssemblyBean() {
        //init();
        preRenderView();
    }

   // @PostConstruct
    public void init() {
        preRenderView();
    }
    public void preRenderView() {
        linhasDeMontagens = findAllAssemblysLinhasDeMontagens() ;
    }

    private List<Assembly> findAllAssemblys() {
        return a.findAllAssemblies();
    }
    private List<LinhasDeMontagens> findAllAssemblysLinhasDeMontagens() {
        return linhasDeMontagensDao.findAll();
    }
    Integer number = 0;
    String caminho;
    List<LinhasDeMontagens> linhasDeMontagens = new ArrayList<>();
    List<Assembly> assemblyList = new ArrayList<>();
    Assembly assembly = new Assembly();
    ConversationListFromFile c = new ConversationListFromFile();
    AssemblyController a = new AssemblyController();
    @PersistenceContext
    private EntityManager em = JPAUtil.getEntityManager();
    LinhasDeMontagensDao linhasDeMontagensDao = new LinhasDeMontagensDao(em);
    AssemblyDao assemblyDao = new AssemblyDao(em);

    public List<LinhasDeMontagens> getLinhasDeMontagens() { return linhasDeMontagens; }
    public void setLinhasDeMontagens(List<LinhasDeMontagens> linhasDeMontagens) { this.linhasDeMontagens = linhasDeMontagens; }
    public Assembly getAssembly() { return assembly; }
    public List<Assembly> getAssemblyList() { return assemblyList; }
    public void setAssemblyList(List<Assembly> assemblyList) {
        this.assemblyList = assemblyList;
    }
    public String getCaminho() {
        return caminho;
    }
    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }

    public void populaInput() throws Exception {

        if (c.getConversationListFromFile(caminho).isEmpty()){
            setLinhasDeMontagens(a.findAllMontagens());
            setAssemblyList(a.findAllAssemblies());
            em.close();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "O arquivo não contém um conteúdo válido, seu conteúdo deve conter uma descrição da atividade e o tempo medido em minutos"));
        }else {
            number++;
            a.populaMontagens(number);
            c.getConversationListFromFile(caminho);
            a.verificaLinha(c.getConversationList());
            setLinhasDeMontagens(a.findAllMontagens());
            setAssemblyList(a.findAllAssemblies());
            em.close();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso!", "Arquivo enviado com sucesso!"));
            caminho = null;
        }
    }
    public String limparDados(){
        assemblyDao.excluirTodos();;
        linhasDeMontagensDao.excluirTodos();
        preRenderView();
        return "/index.xhtml";
    }
    private UploadedFile file;
    public UploadedFile getFile() {
        return file;
    }
    public void setFile(UploadedFile file) {
        this.file = file;
    }
    public void upload() {
        try {
            if (file == null){
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Selecione um arquivo para fazer o upload."));
                return;
            } String fileName = file.getFileName();
            if (!fileName.endsWith(".txt")) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Selecione um arquivo do tipo TXT."));
                return;
            }
            byte[] conteudo = file.getContent();
            File arquivoTemporario = File.createTempFile("arquivo", ".tmp");
            FileOutputStream saida = new FileOutputStream(arquivoTemporario);
            saida.write(conteudo);
            saida.close();
            String caminhoCompleto = arquivoTemporario.getAbsolutePath();
            System.out.println("Caminho completo do arquivo: " + caminhoCompleto);
            caminho = caminhoCompleto;
            populaInput();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
