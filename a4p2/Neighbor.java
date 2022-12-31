public class Neighbor{
    int userId;
    double pearsonValue;
    double rating;

    public Neighbor(int u, double p){
        userId = u;
        pearsonValue = p;
    }

    public Neighbor(int u, double r, double p){
        userId = u;
        pearsonValue = p;
        rating = r;
    }

    public double getPvalue(){return pearsonValue;}
    public int getId(){return userId;}
    public double getRating(){return rating;}
}
