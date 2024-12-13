package net.apuig.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "products", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "product_name_unique"))
public class Product
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // control concurrent modification of available stock
    @Version
    private int version;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Float price;

    @Column(nullable = false)
    private Integer available;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyType currency;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column
    private String imageUrl;

    public Product(String name, Float price, CurrencyType currency, Category category,
        String imageUrl, Integer available)
    {
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.category = category;
        this.imageUrl = imageUrl;
        this.available = available;
    }

    public Product(String name, Float price, CurrencyType currency, Category category,
        Integer available)
    {
        this(name, price, currency, category, null, available);
    }

    public Product()
    {
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Float getPrice()
    {
        return price;
    }

    public void setPrice(Float price)
    {
        this.price = price;
    }

    public CurrencyType getCurrency()
    {
        return currency;
    }

    public void setCurrency(CurrencyType currency)
    {
        this.currency = currency;
    }

    public Category getCategory()
    {
        return category;
    }

    public void setCategory(Category category)
    {
        this.category = category;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public Integer getAvailable()
    {
        return available;
    }

    public void setAvailable(Integer available)
    {
        this.available = available;
    }
}
