public class User implements Comparable<User>{
    private String name;
    private Character sex;
    private Integer age;

    public User(String name, Character sex, Integer age) {
        this.name = name;
        this.sex = sex;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Character getSex() {
        return sex;
    }

    public void setSex(Character sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return
                "name='" + name + '\'' +
                ", sex=" + sex +
                ", age=" + age ;
    }

    @Override
    public int compareTo(User user) {
        return this.age.compareTo(user.getAge());
    }
}
