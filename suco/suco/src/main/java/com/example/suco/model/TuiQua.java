// package com.example.suco.model;

// import jakarta.persistence.*;

// @Entity
// @Table(
//     name = "tui_qua",
//     uniqueConstraints = {
//         @UniqueConstraint(columnNames = {"userId", "quaId"})
//     }
// )
// public class TuiQua {
//         @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     private String userId;

//     private Long quaId;

//     @Column(name = "so_luong")
//     private Integer soLuong = 1;



//     // Getters & Setters
//     public Long getId() { return id; }
//     public void setId(Long id) { this.id = id; }
//     public String getUserId() { return userId; }
//     public void setUserId(String userId) { this.userId = userId; }
//     public Long getQuaId() { return quaId; }
//     public void setQuaId(Long quaId) { this.quaId = quaId; }

//     public Integer getSoLuong() { return soLuong; }
//     public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
   
// }

package com.example.suco.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "tui_qua",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "quaId"})
    }
)
public class TuiQua {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userId")
    private String userId;

    @Column(name = "quaId")
    private Long quaId;

    @Column(name = "so_luong")
    private Integer soLuong = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "userId",
        referencedColumnName = "uid",
        insertable = false,
        updatable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "quaId",
        referencedColumnName = "id",
        insertable = false,
        updatable = false
    )
    private Qua qua;

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getQuaId() {
        return quaId;
    }

    public void setQuaId(Long quaId) {
        this.quaId = quaId;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Qua getQua() {
        return qua;
    }

    public void setQua(Qua qua) {
        this.qua = qua;
    }
}