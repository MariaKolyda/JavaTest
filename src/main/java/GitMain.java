import domain.Project;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GitMain {

    public static void main(String[] args) throws IOException {
        Project project = new Project("https://github.com/eclipse-researchlabs/smartclide-service-creation-theia",
                "C:\\Users\\kolid\\eclipse-workspace\\JavaTest");

        Git git = cloneRepository(project, "");

        List<String> commitIds = new ArrayList<>();
        try {
            String treeName = getHeadName(git.getRepository());
            for (RevCommit commit : git.log().add(git.getRepository().resolve(treeName)).call())
                commitIds.add(commit.getName());
        } catch (Exception ignored) {
        }

        commitIds.forEach(x -> System.out.println(x));

        System.out.println("----");

      git.getRepository().getTags().forEach((s, ref) -> System.out.println(s));

        git.close();
        FileUtils.deleteDirectory(new File(project.getClonePath()));
    }

    public static String getHeadName(Repository repo) {
        String result = null;
        try {
            ObjectId id = repo.resolve(Constants.HEAD);
            result = id.getName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static Git cloneRepository(Project project, String accessToken) {
        try {
            if (Objects.isNull(accessToken))
                return Git.cloneRepository()
                        .setURI(project.getUrl())
                        .setDirectory(new File(project.getClonePath()))
                        .call();
            else {
                return Git.cloneRepository()
                        .setURI(project.getUrl())
                        .setDirectory(new File(project.getClonePath()))
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(accessToken, ""))
                        .call();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
